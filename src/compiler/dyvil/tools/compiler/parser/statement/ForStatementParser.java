package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.statement.ForStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ForStatementParser extends Parser implements IValued
{
	public static final int	FOR				= 1;
	public static final int	TYPE			= 2;
	public static final int	VARIABLE		= 4;
	public static final int	SEPERATOR		= 8;
	public static final int	VARIABLE_END	= 16;
	public static final int	CONDITION_END	= 32;
	public static final int	FOR_END			= 64;
	public static final int	STATEMENT		= 128;
	public static final int	STATEMENT_END	= 256;
	
	public ForStatement		forStatement;
	
	public ForStatementParser(ForStatement forStatement)
	{
		this.forStatement = forStatement;
		this.mode = FOR;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == FOR)
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				this.mode = TYPE;
				return true;
			}
			return false;
		}
		if (this.mode == TYPE)
		{
			if (type == Tokens.SEMICOLON)
			{
				// Condition
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return true;
			}
			
			pm.pushParser(new TypeParser(this.forStatement), true);
			this.mode = VARIABLE;
			return true;
		}
		if (this.mode == VARIABLE)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.forStatement.variable.setName(token.value());
			}
			else
			{
				throw new SyntaxError(token, "Invalid Variable Name");
			}
			
			this.mode = SEPERATOR;
			return true;
		}
		if (this.mode == SEPERATOR)
		{
			if (type == Tokens.EQUALS)
			{
				this.mode = VARIABLE_END;
				pm.pushParser(new ExpressionParser(this));
				return true;
			}
			if (type == Tokens.COLON)
			{
				this.mode = FOR_END;
				this.forStatement.type = 3;
				pm.pushParser(new ExpressionParser(this));
				return true;
			}
		}
		if (this.mode == VARIABLE_END)
		{
			if (type == Tokens.SEMICOLON)
			{
				if (token.next().isType(Tokens.SEMICOLON))
				{
					this.mode = CONDITION_END;
					return true;
				}
				
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return true;
			}
		}
		if (this.mode == CONDITION_END)
		{
			if (type == Tokens.SEMICOLON)
			{
				if (token.next().isType(Tokens.SEMICOLON))
				{
					this.mode = FOR_END;
					return true;
				}
				
				pm.pushParser(new ExpressionParser(this));
				this.mode = FOR_END;
				return true;
			}
			return false;
		}
		if (this.mode == FOR_END)
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.mode = STATEMENT;
				return true;
			}
			return false;
		}
		if (this.mode == STATEMENT)
		{
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return true;
			}
			
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = STATEMENT_END;
			return true;
		}
		if (this.mode == STATEMENT_END)
		{
			pm.popParser(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == VARIABLE_END)
		{
			this.forStatement.variable.value = value;
		}
		else if (this.mode == CONDITION_END)
		{
			this.forStatement.condition = value;
		}
		else if (this.mode == FOR_END)
		{
			if (this.forStatement.type != 0)
			{
				this.forStatement.variable.value = value;
			}
			else
			{
				this.forStatement.update = value;
			}
		}
		else if (this.mode == STATEMENT_END)
		{
			this.forStatement.then = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
}
