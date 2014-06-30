package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ValueParser;

public class StatementParser extends Parser implements IImplementable, IField
{
	private static final int	IF		= 1;
	private static final int	THEN	= 2;
	private static final int	ELSE	= 3;
	private static final int	FOR		= 4;
	
	protected IImplementable	implementable;
	private IStatement			statement;
	
	public StatementParser(IImplementable implementable)
	{
		this.implementable = implementable;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value) || "}".equals(value))
		{
			jcp.popParser();
			return true;
		}
		else if ("{".equals(value))
		{
			StatementList statement = new StatementList();
			this.addStatement(statement);
			jcp.pushParser(new StatementParser(statement));
			return true;
		}
		else if ("if".equals(value))
		{
			if (this.mode == 0)
			{
				IfStatement statement = new IfStatement();
				this.addStatement(statement);
				this.mode = IF;
				return true;
			}
		}
		else if ("(".equals(value))
		{
			if (this.mode == IF)
			{
				jcp.pushParser(new ValueParser(this));
				return true;
			}
		}
		else if (")".equals(value))
		{
			if (this.mode == IF)
			{
				this.mode = THEN;
				return true;
			}
		}
		else if ("else".equals(value))
		{
			if (this.mode == THEN)
			{
				this.mode = ELSE;
				jcp.pushParser(new StatementParser(this));
				return true;
			}
		}
		
		return false;
	}
	
	private void addStatement(IStatement statement)
	{
		if (this.statement == null)
		{
			this.statement = statement;
		}
		else
		{
			StatementList list = new StatementList();
			list.addStatement(this.statement);
			list.addStatement(statement);
			this.statement = list;
		}
	}
	
	@Override
	public void setStatement(IStatement statement)
	{
		if (this.mode == THEN)
		{
			((IfStatement) this.statement).setThen(statement);
		}
		else if (this.mode == ELSE)
		{
			((IfStatement) this.statement).setElseThen(statement);
		}
	}
	
	@Override
	public IStatement getStatement()
	{
		return null;
	}
	
	@Override
	public void setValue(Object value)
	{
		if (this.mode == IF)
		{
			((IfStatement) this.statement).setValue(value);
		}
	}
	
	@Override
	public Object getValue()
	{
		return null;
	}
}
