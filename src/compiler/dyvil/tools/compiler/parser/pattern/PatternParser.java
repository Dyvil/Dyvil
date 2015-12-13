package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.pattern.*;
import dyvil.tools.compiler.ast.pattern.constant.*;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PatternParser extends Parser
{
	private static final int END            = 0;
	private static final int PATTERN        = 1;
	private static final int NEGATIVE_NUMBER = 2;
	private static final int TUPLE_END      = 4;
	private static final int CASE_CLASS_END = 8;
	
	protected IPatternConsumer consumer;
	
	private IPattern pattern;
	
	public PatternParser(IPatternConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = PATTERN;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (this.mode == END)
		{
			if (type == DyvilKeywords.AS)
			{
				TypeCheckPattern tcp = new TypeCheckPattern(token.raw(), this.pattern);
				this.pattern = tcp;
				pm.pushParser(pm.newTypeParser(tcp));
				return;
			}
			
			pm.popParser(true);
			if (this.pattern != null)
			{
				this.consumer.setPattern(this.pattern);
			}
			return;
		}
		
		switch (this.mode)
		{
		case PATTERN:
			if (ParserUtil.isIdentifier(type))
			{
				if (token.nameValue() == Names.minus)
				{
					this.mode = NEGATIVE_NUMBER;
					return;
				}

				final IToken next = token.next();
				if (next.type() == BaseSymbols.OPEN_PARENTHESIS)
				{
					CaseClassPattern ccp = new CaseClassPattern(token.raw());
					ccp.setType(new NamedType(token.raw(), token.nameValue()));
					pm.pushParser(new PatternListParser(ccp));
					pm.skip();
					this.pattern = ccp;
					this.mode = CASE_CLASS_END;
					return;
				}
				
				pm.report(next, "pattern.case_class.open_paren");
				return;
			}
			if (type == DyvilKeywords.VAR)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					this.pattern = new BindingPattern(next.raw(), next.nameValue());
					this.mode = END;
					pm.skip();
					return;
				}
				
				pm.report(next, "pattern.binding.identifier");
				return;
			}
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				TuplePattern tp = new TuplePattern(token);
				this.pattern = tp;
				this.mode = TUPLE_END;
				pm.pushParser(new PatternListParser(tp));
				return;
			}
			IPattern p = parsePrimitive(token, type);
			if (p != null)
			{
				this.pattern = p;
				this.mode = END;
				return;
			}
			pm.report(MarkerMessages.createSyntaxError(token, "pattern.invalid", token.toString()));
			return;
		case NEGATIVE_NUMBER:
			switch (type)
			{
			case Tokens.INT:
				this.pattern = new IntPattern(token.prev().to(token), -token.intValue());
				this.mode = END;
				return;
			case Tokens.LONG:
				this.pattern = new LongPattern(token.prev().to(token), -token.intValue());
				this.mode = END;
				return;
			case Tokens.FLOAT:
				this.pattern = new FloatPattern(token.prev().to(token), -token.intValue());
				this.mode = END;
				return;
			case Tokens.DOUBLE:
				this.pattern = new DoublePattern(token.prev().to(token), -token.intValue());
				this.mode = END;
				return;
			default:
				pm.report(token, "pattern.number.negative");
				this.mode = END;
				pm.reparse();
				return;
			}
		case TUPLE_END:
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.pattern.expandPosition(token);
				this.consumer.setPattern(this.pattern);
				pm.popParser();
				return;
			}
			this.pattern.expandPosition(token.prev());
			this.consumer.setPattern(this.pattern);
			pm.popParser(true);
			pm.report(token, "pattern.tuple.close_paren");
			return;
		case CASE_CLASS_END:
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.consumer.setPattern(this.pattern);
				pm.popParser();
				return;
			}
			this.consumer.setPattern(this.pattern);
			pm.popParser(true);
			pm.report(token, "pattern.case_class.close_paren");
		}
	}
	
	public static IPattern parsePrimitive(IToken token, int type)
	{
		switch (type)
		{
		case DyvilKeywords.TRUE:
			return new BooleanPattern(token.raw(), true);
		case DyvilKeywords.FALSE:
			return new BooleanPattern(token.raw(), false);
		case DyvilSymbols.WILDCARD:
			return new WildcardPattern(token.raw());
		case DyvilKeywords.NULL:
			return new NullPattern(token.raw());
		case Tokens.STRING:
			return new StringPattern(token.raw(), token.stringValue());
		case Tokens.SINGLE_QUOTED_STRING:
			return new CharPattern(token.raw(), token.stringValue());
		case Tokens.INT:
			return new IntPattern(token.raw(), token.intValue());
		case Tokens.LONG:
			return new LongPattern(token.raw(), token.longValue());
		case Tokens.FLOAT:
			return new FloatPattern(token.raw(), token.floatValue());
		case Tokens.DOUBLE:
			return new DoublePattern(token.raw(), token.doubleValue());
		}
		return null;
	}
}
