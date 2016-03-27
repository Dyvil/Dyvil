package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class OperatorParser extends Parser
{
	private static final int TYPE          = 1;
	private static final int OPERATOR      = 2;
	private static final int OPEN_BRACKET  = 4;
	private static final int PROPERTY      = 8;
	private static final int PRECEDENCE    = 16;
	private static final int ASSOCIATIVITY = 32;
	private static final int COMMA         = 64;
	
	public static final Name associativity = Name.getQualified("associativity");
	public static final Name precedence    = Name.getQualified("precedence");
	public static final Name none          = Name.getQualified("none");
	public static final Name left          = Name.getQualified("left");
	public static final Name right         = Name.getQualified("right");
	
	protected IOperatorMap map;
	private   int          type;
	private   Operator     operator;
	
	public OperatorParser(IOperatorMap map, int type)
	{
		this.map = map;
		
		if (type >= 0)
		{
			this.type = type;
			this.mode = OPERATOR;
		}
		else
		{
			this.mode = TYPE;
		}
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
				this.type = Operator.PREFIX;
				return;
			case DyvilKeywords.POSTFIX:
				this.type = Operator.POSTFIX;
				return;
			case DyvilKeywords.INFIX:
				this.type = Operator.INFIX_NONE;
				return;
			}
			pm.report(token, "operator.type.invalid");
			return;
		case OPERATOR:
			this.mode = OPEN_BRACKET;
			if (type == DyvilKeywords.OPERATOR)
			{
				IToken next = token.next();
				Name name = next.nameValue();
				pm.skip();
				if (name == null)
				{
					pm.report(next, "operator.identifier");
					return;
				}
				
				this.operator = new Operator(name);
				this.operator.type = this.type;
				
				if (this.type == Operator.PREFIX || this.type == Operator.POSTFIX)
				{
					this.operator.precedence = Operator.PREFIX_PRECEDENCE;
				}
				return;
			}
			pm.report(token, "operator.operator");
			return;
		case OPEN_BRACKET:
			switch (this.type)
			{
			case Operator.PREFIX:
				if (type == BaseSymbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != BaseSymbols.SEMICOLON && type != Tokens.EOF)
				{
					pm.report(token, "operator.prefix.semicolon");
					return;
				}
				return;
			case Operator.POSTFIX:
				if (type == BaseSymbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != BaseSymbols.SEMICOLON && type != Tokens.EOF)
				{
					pm.report(token, "operator.postfix.semicolon");
					return;
				}
				return;
			default: // infix
				this.mode = PROPERTY;
				if (type != BaseSymbols.OPEN_CURLY_BRACKET)
				{
					pm.reparse();
					pm.report(token, "operator.infix.open_brace");
					return;
				}
				return;
			}
		case PROPERTY:
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
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
				if (name == left)
				{
					this.setAssociativity(pm, token, Operator.INFIX_LEFT);
					this.mode = COMMA;
					return;
				}
				if (name == none)
				{
					this.setAssociativity(pm, token, Operator.INFIX_NONE);
					this.mode = COMMA;
					return;
				}
				if (name == right)
				{
					this.setAssociativity(pm, token, Operator.INFIX_RIGHT);
					this.mode = COMMA;
					return;
				}
			}
			if ((type & Tokens.INT) != 0)
			{
				this.operator.precedence = token.intValue();
				this.mode = COMMA;
				return;
			}
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
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
			this.operator.precedence = token.intValue();
			return;
		case ASSOCIATIVITY:
			this.mode = COMMA;
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == left)
				{
					this.setAssociativity(pm, token, Operator.INFIX_LEFT);
					return;
				}
				if (name == none)
				{
					this.setAssociativity(pm, token, Operator.INFIX_NONE);
					return;
				}
				if (name == right)
				{
					this.setAssociativity(pm, token, Operator.INFIX_RIGHT);
					return;
				}
			}
			
			pm.reparse();
			pm.report(token, "operator.property.associativity");
		}
	}
	
	private void setAssociativity(IParserManager pm, IToken token, int associativity)
	{
		switch (this.operator.type)
		{
		case Operator.POSTFIX:
			pm.report(token, "operator.postfix.associativity");
			return;
		case Operator.PREFIX:
			pm.report(token, "operator.prefix.associativity");
			return;
		}
		
		this.operator.type = associativity;
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > OPERATOR;
	}
}
