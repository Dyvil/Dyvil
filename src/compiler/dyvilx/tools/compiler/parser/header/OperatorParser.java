package dyvilx.tools.compiler.parser.header;

import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.expression.operator.IOperatorMap;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public final class OperatorParser extends Parser
{
	private static final int TYPE            = 0;
	private static final int OPERATOR        = 1;
	private static final int OPERATOR_SYMBOL = 2;
	private static final int SEPARATOR       = 4;
	private static final int PROPERTY        = 8;
	private static final int PRECEDENCE      = 16;
	private static final int ASSOCIATIVITY   = 32;
	private static final int COMMA           = 64;

	public static final Name associativity = Name.fromRaw("associativity");
	public static final Name precedence    = Name.fromRaw("precedence");
	public static final Name none          = Name.fromRaw("none");
	public static final Name left          = Name.fromRaw("left");
	public static final Name right         = Name.fromRaw("right");

	protected IOperatorMap map;
	private   byte         type;
	private   Operator     operator;

	public OperatorParser(IOperatorMap map)
	{
		this.map = map;
		// this.mode = TYPE;
	}

	public OperatorParser(IOperatorMap map, byte type)
	{
		this.map = map;

		this.type = type;
		this.mode = OPERATOR;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			this.mode = OPERATOR;
			switch (type)
			{
			case DyvilKeywords.PREFIX:
				this.type = IOperator.PREFIX;
				return;
			case DyvilKeywords.POSTFIX:
				this.type = IOperator.POSTFIX;
				return;
			case DyvilKeywords.INFIX:
				this.type = IOperator.INFIX;
				return;
			}
			pm.report(token, "operator.type.invalid");
			return;
		case OPERATOR:
			this.mode = OPERATOR_SYMBOL;
			if (type != DyvilKeywords.OPERATOR)
			{
				pm.reparse();
				pm.report(token, "operator.operator");
			}
			return;
		case OPERATOR_SYMBOL:
		{
			Name name = getOperatorName(pm, token);
			if (name == null)
			{
				pm.report(token, "operator.identifier");
				return;
			}

			this.operator = new Operator(name, this.type);

			// TODO Add 'ternary' keyword?
			if (this.type == IOperator.INFIX || this.type == IOperator.TERNARY)
			{
				name = getOperatorName(pm, token.next());
				if (name != null)
				{
					this.operator.setType(IOperator.TERNARY);
					this.operator.setTernaryName(name);
					pm.skip();
				}
			}
			this.mode = SEPARATOR;
			return;
		}
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				pm.popParser(true);
				this.map.addOperator(this.operator);
				return;
			case Tokens.EOF:
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = PROPERTY;
				return;
			}

			pm.popParser(true);
			this.map.addOperator(this.operator);
			pm.report(token, "operator.separator");
			return;
		case PROPERTY:
			switch (type)
			{
			case Tokens.LETTER_IDENTIFIER:
				final Name name = token.nameValue();
				if (name == precedence)
				{
					this.mode = PRECEDENCE;
					return;
				}
				if (name == associativity)
				{
					this.mode = ASSOCIATIVITY;
					return;
				}
				if (this.parseAssociativity(pm, token, name))
				{
					this.mode = COMMA;
					return;
				}
				break;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			if ((type & Tokens.INT) != 0)
			{
				this.setPrecedence(pm, token, token.intValue());
				this.mode = COMMA;
				return;
			}
			pm.report(Markers.syntaxError(token, "operator.property.invalid", token));
			return;
		case COMMA:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			this.mode = PROPERTY;
			if (type != BaseSymbols.COMMA)
			{
				pm.reparse();
				pm.report(token, "operator.property.comma");
				return;
			}
			return;
		case PRECEDENCE:
			this.mode = COMMA;
			if ((type & Tokens.INT) == 0)
			{
				pm.reparse();
				pm.report(token, "operator.property.precedence");
				return;
			}
			this.setPrecedence(pm, token, token.intValue());
			return;
		case ASSOCIATIVITY:
			this.mode = COMMA;
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				final Name name = token.nameValue();
				if (this.parseAssociativity(pm, token, name))
				{
					return;
				}
			}

			pm.reparse();
			pm.report(token, "operator.property.associativity");
		}
	}

	private static Name getOperatorName(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (type == Tokens.SYMBOL_IDENTIFIER || type == Tokens.SPECIAL_IDENTIFIER)
		{
			return token.nameValue();
		}
		if (type == Tokens.LETTER_IDENTIFIER)
		{
			pm.report(Markers.syntaxWarning(token, "operator.identifier.not_symbol"));
			return token.nameValue();
		}
		if ((type & Tokens.SYMBOL) != 0)
		{
			return Name.from(DyvilSymbols.INSTANCE.toString(type));
		}
		return null;
	}

	public boolean parseAssociativity(IParserManager pm, IToken token, Name name)
	{
		if (name == left)
		{
			this.setAssociativity(pm, token, IOperator.LEFT);
			return true;
		}
		if (name == none)
		{
			this.setAssociativity(pm, token, IOperator.NONE);
			return true;
		}
		if (name == right)
		{
			this.setAssociativity(pm, token, IOperator.RIGHT);
			return true;
		}
		return false;
	}

	private void setAssociativity(IParserManager pm, IToken token, byte associativity)
	{
		switch (this.type)
		{
		case IOperator.POSTFIX:
			pm.report(token, "operator.postfix.associativity");
			return;
		case IOperator.PREFIX:
			pm.report(token, "operator.prefix.associativity");
			return;
		case IOperator.TERNARY:
			pm.report(token, "operator.ternary.associativity");
			return;
		}

		this.operator.setAssociativity(associativity);
	}

	private void setPrecedence(IParserManager pm, IToken token, int precedence)
	{
		switch (this.type)
		{
		case IOperator.POSTFIX:
			pm.report(token, "operator.postfix.precedence");
			return;
		case IOperator.PREFIX:
			pm.report(token, "operator.prefix.precedence");
			return;
		}

		this.operator.setPrecedence(precedence);
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > OPERATOR && super.reportErrors();
	}
}
