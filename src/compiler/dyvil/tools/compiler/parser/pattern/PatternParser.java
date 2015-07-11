package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.pattern.*;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public class PatternParser extends Parser
{
	public static final int		PATTERN			= 1;
	public static final int		ARRAY_END		= 2;
	public static final int		TUPLE_END		= 4;
	public static final int		CASE_CLASS_END	= 8;
	
	protected IPatternConsumer	consumer;
	
	private IPattern			pattern;
	
	public PatternParser(IPatternConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = PATTERN;
	}
	
	@Override
	public void reset()
	{
		this.mode = PATTERN;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == 0 || type == Symbols.COLON)
		{
			if (type == Keywords.AS)
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
		
		if (this.mode == PATTERN)
		{
			if (ParserUtil.isIdentifier(type))
			{
				IToken next = token.next();
				if (next.type() == Symbols.OPEN_PARENTHESIS)
				{
					CaseClassPattern ccp = new CaseClassPattern(token.raw());
					ccp.setType(new NamedType(token.raw(), token.nameValue()));
					pm.pushParser(new PatternListParser(ccp));
					pm.skip();
					this.pattern = ccp;
					this.mode = CASE_CLASS_END;
					return;
				}
				
				throw new SyntaxError(next, "Invalid Case Class Pattern - '(' expected");
			}
			if (type == Keywords.VAR)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()))
				{
					BindingPattern bp = new BindingPattern(next.raw(), next.nameValue());
					this.pattern = bp;
					this.mode = 0;
					pm.skip();
					return;
				}
				
				throw new SyntaxError(next, "Invalid Binding Pattern - Identifier expected");
			}
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.mode = ARRAY_END;
				return;
			}
			if (type == Symbols.OPEN_PARENTHESIS)
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
				this.mode = 0;
				return;
			}
			
			throw new SyntaxError(token, "Invalid Pattern");
		}
		if (this.mode == ARRAY_END)
		{
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.pattern.expandPosition(token);
				this.consumer.setPattern(this.pattern);
				pm.popParser();
				return;
			}
			this.pattern.expandPosition(token.prev());
			this.consumer.setPattern(this.pattern);
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Array Pattern - '}' expected");
		}
		if (this.mode == TUPLE_END)
		{
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				this.pattern.expandPosition(token);
				this.consumer.setPattern(this.pattern);
				pm.popParser();
				return;
			}
			this.pattern.expandPosition(token.prev());
			this.consumer.setPattern(this.pattern);
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Tuple Pattern - ')' expected");
		}
		if (this.mode == CASE_CLASS_END)
		{
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				this.consumer.setPattern(this.pattern);
				pm.popParser();
				return;
			}
			this.consumer.setPattern(this.pattern);
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Case Class Pattern - ')' expected");
		}
	}
	
	public static IPattern parsePrimitive(IToken token, int type) throws SyntaxError
	{
		switch (type)
		{
		case Keywords.TRUE:
			return new BooleanPattern(token.raw(), true);
		case Keywords.FALSE:
			return new BooleanPattern(token.raw(), false);
		case Symbols.WILDCARD:
			return new WildcardPattern(token.raw());
		case Keywords.NULL:
			return new NullPattern(token.raw());
		case Tokens.STRING:
			return new StringPattern(token.raw(), token.stringValue());
		case Tokens.CHAR:
			return new CharPattern(token.raw(), token.charValue());
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
