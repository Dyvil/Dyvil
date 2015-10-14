package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class OperatorParser extends Parser
{
	private static final int	TYPE			= 1;
	private static final int	OPERATOR		= 2;
	private static final int	OPEN_BRACKET	= 4;
	private static final int	PROPERTY		= 8;
	private static final int	PRECEDENCE		= 16;
	private static final int	ASSOCIATIVITY	= 32;
	private static final int	COMMA			= 64;
	
	public static final Name	associativity	= Name.getQualified("associativity");
	public static final Name	precedence		= Name.getQualified("precedence");
	public static final Name	none			= Name.getQualified("none");
	public static final Name	left			= Name.getQualified("left");
	public static final Name	right			= Name.getQualified("right");
	
	protected IOperatorMap	map;
	private int				type;
	private Operator		operator;
	
	public OperatorParser(IOperatorMap map, boolean typeParsed)
	{
		this.map = map;
		
		if (typeParsed)
		{
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
		int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			this.mode = OPERATOR;
			switch (type)
			{
			case Keywords.PREFIX:
				this.type = Operator.PREFIX;
				return;
			case Keywords.POSTFIX:
				this.type = Operator.POSTFIX;
				return;
			case Keywords.INFIX:
				this.type = Operator.INFIX_NONE;
				return;
			}
			pm.report(token, "Invalid Operator - 'infix', 'prefix' or 'postfix' expected");
			return;
		case OPERATOR:
			this.mode = OPEN_BRACKET;
			if (type == Keywords.OPERATOR)
			{
				IToken next = token.next();
				Name name = next.nameValue();
				pm.skip();
				if (name == null)
				{
					pm.report(next, "Invalid Operator - Identifier expected");
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
			pm.report(token, "Invalid Operator - 'operator' expected");
			return;
		case OPEN_BRACKET:
			switch (this.type)
			{
			case Operator.PREFIX:
				if (type == Symbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != Symbols.SEMICOLON)
				{
					pm.report(token, "Invalid Prefix Operator - ';' expected");
					return;
				}
				return;
			case Operator.POSTFIX:
				if (type == Symbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PROPERTY;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != Symbols.SEMICOLON)
				{
					pm.report(token, "Invalid Postfix Operator - ';' expected");
					return;
				}
				return;
			default: // infix
				this.mode = PROPERTY;
				if (type != Symbols.OPEN_CURLY_BRACKET)
				{
					pm.reparse();
					pm.report(token, "Invalid Infix Operator - '{' expected");
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
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			pm.report(token, "Invalid Operator Property - Invalid " + token);
			return;
		case COMMA:
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				this.map.addOperator(this.operator);
				return;
			}
			this.mode = PROPERTY;
			if (type != Symbols.COMMA)
			{
				pm.reparse();
				pm.report(token, "Invalid Infix Operator - ',' expected");
				return;
			}
			return;
		case PRECEDENCE:
			this.mode = COMMA;
			if ((type & Tokens.INT) == 0)
			{
				pm.reparse();
				pm.report(token, "Invalid Operator Precedence - Integer expected");
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
			pm.report(token, "Invalid Operator Associativity - 'left', 'none' or 'right' expected");
			return;
		}
	}
	
	private void setAssociativity(IParserManager pm, IToken token, int associativity)
	{
		switch (this.operator.type)
		{
		case Operator.POSTFIX:
			pm.report(token, "Invalid Postfix Operator - Postfix Operators cannot have an associativity");
			return;
		case Operator.PREFIX:
			pm.report(token, "Invalid Prefix Operator - Prefix Operators cannot have an associativity");
			return;
		}
		
		this.operator.type = associativity;
	}
}
