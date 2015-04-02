package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public final class OperatorParser extends Parser
{
	private static final int	OPEN_BRACKET	= 1;
	private static final int	TYPE			= 2;
	private static final int	COMMA			= 4;
	private static final int	PRECEDENCE		= 8;
	private static final int	CLOSE_BRACKET	= 16;
	
	protected Operator			operator;
	
	public OperatorParser(Operator op)
	{
		this.operator = op;
		this.mode = OPEN_BRACKET;
	}
	
	@Override
	public void reset()
	{
		this.mode = OPEN_BRACKET;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == OPEN_BRACKET)
		{
			this.mode = TYPE;
			if (type != Symbols.OPEN_CURLY_BRACKET)
			{
				throw new SyntaxError(token, "Invalid Operator - '{' expected", true);
			}
			return;
		}
		if (this.mode == TYPE)
		{
			if (type != Tokens.LETTER_IDENTIFIER)
			{
				this.mode = CLOSE_BRACKET;
				throw new SyntaxError(token, "Invalid Operator - Type expected", true);
			}
			Name name = token.nameValue();
			switch (name.qualified)
			{
			case "prefix":
				this.operator.type = (Operator.PREFIX);
				this.mode = CLOSE_BRACKET;
				return;
			case "postfix":
				this.operator.type = (Operator.POSTFIX);
				this.mode = CLOSE_BRACKET;
				return;
			case "none":
				this.operator.type = (Operator.INFIX_NONE);
				this.mode = COMMA;
				return;
			case "left":
				this.operator.type = (Operator.INFIX_LEFT);
				this.mode = COMMA;
				return;
			case "right":
				this.operator.type = (Operator.INFIX_RIGHT);
				this.mode = COMMA;
				return;
			}
		}
		if (this.mode == COMMA)
		{
			this.mode = PRECEDENCE;
			if (type != Symbols.COMMA)
			{
				throw new SyntaxError(token, "Invalid Infix Operator - ',' expected", true);
			}
			return;
		}
		if (this.mode == PRECEDENCE)
		{
			this.mode = CLOSE_BRACKET;
			if ((type & Tokens.INT) == 0)
			{
				throw new SyntaxError(token, "Invalid Infix Operator - Integer Precedence expected", true);
			}
			this.operator.precedence = token.intValue();
			return;
		}
		if (this.mode == CLOSE_BRACKET)
		{
			pm.popParser();
			if (type != Symbols.CLOSE_CURLY_BRACKET)
			{
				throw new SyntaxError(token, "Invalid Operator - '}' expected");
			}
			return;
		}
	}
}
