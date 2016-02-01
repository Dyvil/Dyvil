package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.pattern.*;
import dyvil.tools.compiler.ast.pattern.constant.*;
import dyvil.tools.compiler.ast.pattern.operator.AndPattern;
import dyvil.tools.compiler.ast.pattern.operator.OrPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class PatternParser extends Parser implements ITypeConsumer
{
	private static final int PATTERN         = 0;
	private static final int NEGATIVE_NUMBER = 1;
	private static final int TYPE_END        = 2;
	private static final int TUPLE_END       = 4;
	private static final int CASE_CLASS_END  = 8;

	private static final int OPERATOR_OR  = 1;
	private static final int OPERATOR_AND = 2;
	
	protected IPatternConsumer consumer;
	
	private IPattern pattern;

	private int   operator;
	private IType type;
	
	public PatternParser(IPatternConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = PATTERN;
	}

	protected PatternParser(IPatternConsumer consumer, int operator)
	{
		this.consumer = consumer;
		this.operator = operator;
		this.mode = PATTERN;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case END:
			if (type == DyvilKeywords.AS)
			{
				final TypeCheckPattern typeCheck = new TypeCheckPattern(token.raw(), this.pattern);
				this.pattern = typeCheck;
				pm.pushParser(pm.newTypeParser(typeCheck));
				return;
			}

			if (ParserUtil.isIdentifier(type))
			{
				final Name name = token.nameValue();
				if (name == Names.bar)
				{
					if (this.checkPrecedence(OPERATOR_OR))
					{
						this.endPattern(pm);
						return;
					}

					final OrPattern orPattern = new OrPattern(this.pattern, token.raw(), null);
					this.pattern = orPattern;
					pm.pushParser(new PatternParser(orPattern::setRight, OPERATOR_OR));
					return;
				}

				if (name == Names.amp)
				{
					if (this.checkPrecedence(OPERATOR_AND))
					{
						this.endPattern(pm);
						return;
					}

					final AndPattern andPattern = new AndPattern(this.pattern, token.raw(), null);
					this.pattern = andPattern;
					pm.pushParser(new PatternParser(andPattern::setRight, OPERATOR_AND));
					return;
				}
			}

			this.endPattern(pm);
			return;
		case PATTERN:
			if (ParserUtil.isIdentifier(type))
			{
				if (token.nameValue() == Names.minus)
				{
					this.mode = NEGATIVE_NUMBER;
					return;
				}

				this.mode = TYPE_END;
				pm.pushParser(pm.newTypeParser(this), true);
				return;
			}
			if (type == DyvilKeywords.VAR)
			{
				final IToken nextToken = token.next();
				if (ParserUtil.isIdentifier(nextToken.type()))
				{
					this.pattern = new BindingPattern(nextToken.raw(), nextToken.nameValue());
					this.mode = END;
					pm.skip();
					return;
				}
				
				pm.report(nextToken, "pattern.binding.identifier");
				return;
			}
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				final TuplePattern tuplePattern = new TuplePattern(token);
				this.pattern = tuplePattern;
				this.mode = TUPLE_END;
				pm.pushParser(new PatternListParser(tuplePattern));
				return;
			}
			final IPattern primitive = parsePrimitive(token, type);
			if (primitive != null)
			{
				this.pattern = primitive;
				this.mode = END;
				return;
			}
			pm.report(Markers.syntaxError(token, "pattern.invalid", token.toString()));
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
		case TYPE_END:
		{
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				final CaseClassPattern ccp = new CaseClassPattern(token, this.type);
				pm.pushParser(new PatternListParser(ccp));
				this.pattern = ccp;
				this.mode = CASE_CLASS_END;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				this.pattern = new BindingPattern(token.raw(), this.type, token.nameValue());
				this.mode = END;
				return;
			}

			this.pattern = new ObjectPattern(this.type.getPosition(), this.type);
			this.mode = END;
			pm.reparse();
			return;
		}
		case TUPLE_END:
			this.mode = END;
			this.pattern.expandPosition(token);
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "pattern.tuple.close_paren");
			}
			return;
		case CASE_CLASS_END:
			this.mode = END;
			this.pattern.expandPosition(token);
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "pattern.case_class.close_paren");
			}
			return;
		}
	}

	private void endPattern(IParserManager pm)
	{
		pm.popParser(true);
		if (this.pattern != null)
		{
			this.consumer.setPattern(this.pattern);
		}
	}
	
	private boolean checkPrecedence(int operator)
	{
		return this.operator != 0 && this.operator > operator;
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

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}
