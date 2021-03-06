package dyvilx.tools.compiler.parser.pattern;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.pattern.BindingPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvilx.tools.compiler.ast.pattern.constant.*;
import dyvilx.tools.compiler.ast.pattern.object.EnumPattern;
import dyvilx.tools.compiler.ast.pattern.object.ObjectPattern;
import dyvilx.tools.compiler.ast.pattern.object.TuplePattern;
import dyvilx.tools.compiler.ast.pattern.object.UnapplyPattern;
import dyvilx.tools.compiler.ast.pattern.operator.AndPattern;
import dyvilx.tools.compiler.ast.pattern.operator.OrPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class PatternParser extends Parser implements Consumer<IType>
{
	// =============== Constants ===============

	private static final int PATTERN         = 0;
	private static final int NEGATIVE_NUMBER = 1;
	private static final int ENUM_IDENTIFIER = 2;
	private static final int TYPE_END        = 3;
	private static final int TUPLE_END       = 4;
	private static final int CASE_CLASS_END  = 5;

	private static final int OPERATOR_OR  = 1;
	private static final int OPERATOR_AND = 2;

	// =============== Fields ===============

	protected final Consumer<Pattern> consumer;

	private Pattern pattern;

	private int   operator;
	private IType type;

	// =============== Constructors ===============

	public PatternParser(Consumer<Pattern> consumer)
	{
		this(consumer, 0);
	}

	protected PatternParser(Consumer<Pattern> consumer, int operator)
	{
		this.consumer = consumer;
		this.operator = operator;
		// this.mode = PATTERN;
	}

	// =============== Methods ===============

	@Override
	public void accept(IType type)
	{
		this.type = type;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case PATTERN:
			switch (type)
			{
			case DyvilKeywords.TRUE:
				this.pattern = new BooleanPattern(token.raw(), true);
				this.mode = END;
				return;
			case DyvilKeywords.FALSE:
				this.pattern = new BooleanPattern(token.raw(), false);
				this.mode = END;
				return;
			case DyvilSymbols.UNDERSCORE:
				this.pattern = new WildcardPattern(token.raw());
				this.mode = END;
				return;
			case DyvilKeywords.NULL:
				this.pattern = new NullPattern(token.raw());
				this.mode = END;
				return;
			case Tokens.STRING:
				this.pattern = new StringPattern(token.raw(), token.stringValue());
				this.mode = END;
				return;
			case Tokens.SINGLE_QUOTED_STRING:
				this.pattern = new CharPattern(token.raw(), token.stringValue());
				this.mode = END;
				return;
			case Tokens.VERBATIM_CHAR:
				this.pattern = new CharPattern(token.raw(), token.stringValue(), true);
				this.mode = END;
				return;
			case Tokens.INT:
				this.pattern = new IntPattern(token.raw(), token.intValue());
				this.mode = END;
				return;
			case Tokens.LONG:
				this.pattern = new LongPattern(token.raw(), token.longValue());
				this.mode = END;
				return;
			case Tokens.FLOAT:
				this.pattern = new FloatPattern(token.raw(), token.floatValue());
				this.mode = END;
				return;
			case Tokens.DOUBLE:
				this.pattern = new DoublePattern(token.raw(), token.doubleValue());
				this.mode = END;
				return;
			case DyvilKeywords.VAR:
			case DyvilKeywords.LET:
				final BindingPattern pattern = new BindingPattern();
				pm.pushParser(new DataMemberParser<>(pattern), true);
				this.pattern = pattern;
				this.mode = END;
				return;
			case BaseSymbols.OPEN_PARENTHESIS:
				final TuplePattern tuplePattern = new TuplePattern(token);
				this.pattern = tuplePattern;
				this.mode = TUPLE_END;
				pm.pushParser(new PatternListParser(tuplePattern::add));
				return;
			case BaseSymbols.DOT:
				this.mode = ENUM_IDENTIFIER;
				return;
				// Error cases
			case BaseSymbols.COLON:
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
			case DyvilKeywords.IF:
				this.end(pm, token);
				return;
			}
			if (Tokens.isIdentifier(type))
			{
				if (token.nameValue() == Names.$minus)
				{
					this.mode = NEGATIVE_NUMBER;
					return;
				}

				this.mode = TYPE_END;
				pm.pushParser(new TypeParser(this).withFlags(TypeParser.NAMED_ONLY), true);
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
		case ENUM_IDENTIFIER:
			if (Tokens.isIdentifier(type))
			{
				this.pattern = new EnumPattern(token.raw(), token.nameValue());
				this.mode = END;
				return;
			}

			pm.report(token, "pattern.enum.identifier");
			return;
		case TYPE_END:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				// TYPE ( ...
				// => Unapply pattern
				final UnapplyPattern unapplyPattern = new UnapplyPattern(token, this.type);
				pm.pushParser(new PatternListParser(unapplyPattern::add));
				this.pattern = unapplyPattern;
				this.mode = CASE_CLASS_END;
				return;
			}

			// TYPE
			// => Object Pattern
			this.pattern = new ObjectPattern(this.type.getPosition(), this.type);
			this.mode = END;
			pm.reparse();
			return;
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
		case END:
			if (type == DyvilKeywords.AS)
			{
				final TypeCheckPattern typeCheck = new TypeCheckPattern(token.raw(), this.pattern);
				this.pattern = typeCheck;
				pm.pushParser(new TypeParser(typeCheck::setType));
				return;
			}

			if (Tokens.isIdentifier(type))
			{
				final Name name = token.nameValue();
				if (name == Names.$bar)
				{
					if (this.checkPrecedence(OPERATOR_OR))
					{
						this.end(pm, token);
						return;
					}

					final OrPattern orPattern = new OrPattern(this.pattern, token.raw(), null);
					this.pattern = orPattern;
					pm.pushParser(new PatternParser(orPattern::setRight, OPERATOR_OR));
					return;
				}

				if (name == Names.$amp)
				{
					if (this.checkPrecedence(OPERATOR_AND))
					{
						this.end(pm, token);
						return;
					}

					final AndPattern andPattern = new AndPattern(this.pattern, token.raw(), null);
					this.pattern = andPattern;
					pm.pushParser(new PatternParser(andPattern::setRight, OPERATOR_AND));
					return;
				}
			}

			this.end(pm, token);
		}
	}

	private void end(IParserManager pm, IToken token)
	{
		if (this.pattern != null)
		{
			this.consumer.accept(this.pattern);
		}
		else
		{
			pm.report(Markers.syntaxError(token, "pattern.expected", token));
			this.consumer.accept(new WildcardPattern(SourcePosition.after(token.prev())));
		}
		pm.popParser(true);
	}

	private boolean checkPrecedence(int operator)
	{
		return this.operator != 0 && this.operator > operator;
	}
}
