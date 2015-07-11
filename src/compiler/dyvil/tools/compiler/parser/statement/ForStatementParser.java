package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.ForStatement;
import dyvil.tools.compiler.ast.statement.foreach.ForEachStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

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
	
	public IValueConsumer	field;
	
	private Variable		variable;
	private IValue			update;
	private IValue			condition;
	private IValue			action;
	private boolean			forEach;
	
	public ForStatementParser(IValueConsumer field)
	{
		this.field = field;
		this.mode = FOR;
	}
	
	@Override
	public void reset()
	{
		this.mode = FOR;
	}
	
	private IValue makeForStatement()
	{
		if (this.forEach)
		{
			return new ForEachStatement(this.variable, this.action);
		}
		return new ForStatement(this.variable, this.condition, this.update, this.action);
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == FOR)
		{
			this.mode = TYPE;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid For Statement - '(' expected", true);
		}
		if (this.mode == TYPE)
		{
			if (type == Symbols.SEMICOLON)
			{
				// Condition
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			
			this.variable = new Variable();
			pm.pushParser(pm.newTypeParser(this.variable), true);
			this.mode = VARIABLE;
			return;
		}
		if (this.mode == VARIABLE)
		{
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				this.variable.name = token.nameValue();
				this.variable.position = token.raw();
				return;
			}
			
			throw new SyntaxError(token, "Invalid For statement - Variable Name expected", true);
		}
		if (this.mode == SEPERATOR)
		{
			if (type == Symbols.COLON)
			{
				this.mode = FOR_END;
				this.forEach = true;
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}
			
			this.mode = VARIABLE_END;
			if (type == Symbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.variable));
				return;
			}
			throw new SyntaxError(token, "Invalid For Statement - ';' or ':' expected", true);
		}
		if (this.mode == VARIABLE_END)
		{
			this.mode = CONDITION_END;
			if (type == Symbols.SEMICOLON)
			{
				if (token.next().type() == Symbols.SEMICOLON)
				{
					return;
				}
				
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			throw new SyntaxError(token, "Invalid for statement - ';' expected", true);
		}
		if (this.mode == CONDITION_END)
		{
			this.mode = FOR_END;
			if (type == Symbols.SEMICOLON)
			{
				if (token.next().type() == Symbols.SEMICOLON)
				{
					return;
				}
				
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			throw new SyntaxError(token, "Invalid for statement - ';' expected", true);
		}
		if (this.mode == FOR_END)
		{
			this.mode = STATEMENT;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid for statement - ')' expected", true);
		}
		if (this.mode == STATEMENT)
		{
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				this.field.setValue(this.makeForStatement());
				return;
			}
			
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = STATEMENT_END;
			return;
		}
		if (this.mode == STATEMENT_END)
		{
			pm.popParser(true);
			this.field.setValue(this.makeForStatement());
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == VARIABLE_END)
		{
			this.variable.value = value;
		}
		else if (this.mode == CONDITION_END)
		{
			this.condition = value;
		}
		else if (this.mode == FOR_END)
		{
			if (this.forEach)
			{
				this.variable.value = value;
			}
			else
			{
				this.update = value;
			}
		}
		else if (this.mode == STATEMENT_END)
		{
			this.action = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
