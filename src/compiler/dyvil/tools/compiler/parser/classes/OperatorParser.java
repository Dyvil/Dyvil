package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public final class OperatorParser extends Parser
{
	private static final int	TYPE			= 1;
	private static final int	OPERATOR		= 2;
	private static final int	OPEN_BRACKET	= 4;
	private static final int	ASSOCIATIVITY	= 8;
	private static final int	COMMA			= 16;
	private static final int	PRECEDENCE		= 32;
	private static final int	CLOSE_BRACKET	= 64;
	
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
			pm.report(new SyntaxError(token, "Invalid Operator - 'infix', 'prefix' or 'postfix' expected"));
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
					pm.report(new SyntaxError(next, "Invalid Operator - Identifier expected"));
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
			pm.report(new SyntaxError(token, "Invalid Operator - 'operator' expected"));
			return;
		case OPEN_BRACKET:
			switch (this.type)
			{
			case Operator.PREFIX:
				if (type == Symbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PRECEDENCE;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != Symbols.SEMICOLON)
				{
					pm.report(new SyntaxError(token, "Invalid Prefix Operator - ';' expected"));
					return;
				}
				return;
			case Operator.POSTFIX:
				if (type == Symbols.OPEN_CURLY_BRACKET)
				{
					this.mode = PRECEDENCE;
					return;
				}
				pm.popParser();
				this.map.addOperator(this.operator);
				if (type != Symbols.SEMICOLON)
				{
					pm.report(new SyntaxError(token, "Invalid Postfix Operator - ';' expected"));
					return;
				}
				return;
			default: // infix
				this.mode = ASSOCIATIVITY;
				if (type != Symbols.OPEN_CURLY_BRACKET)
				{
					pm.reparse();
					pm.report(new SyntaxError(token, "Invalid Infix Operator - '{' expected"));
					return;
				}
				return;
			}
		case ASSOCIATIVITY:
			if (token.type() == Tokens.LETTER_IDENTIFIER)
			{
				switch (token.nameValue().qualified)
				{
				case "none":
					this.operator.type = Operator.INFIX_NONE;
					this.mode = COMMA;
					return;
				case "left":
					this.operator.type = Operator.INFIX_LEFT;
					this.mode = COMMA;
					return;
				case "right":
					this.operator.type = Operator.INFIX_RIGHT;
					this.mode = COMMA;
					return;
				}
			}
			pm.report(new SyntaxError(token, "Invalid Operator Type - Invalid " + token));
			return;
		case COMMA:
			this.mode = PRECEDENCE;
			if (type != Symbols.COMMA)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Infix Operator - ',' expected"));
				return;
			}
			return;
		case PRECEDENCE:
			this.mode = CLOSE_BRACKET;
			if ((type & Tokens.INT) == 0)
			{
				pm.reparse();
				pm.report(new SyntaxError(token, "Invalid Operator - Integer Precedence expected"));
				return;
			}
			this.operator.precedence = token.intValue();
			return;
		case CLOSE_BRACKET:
			pm.popParser();
			this.map.addOperator(this.operator);
			if (type != Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.report(new SyntaxError(token, "Invalid Operator - '}' expected"));
				return;
			}
			return;
		}
	}
}
