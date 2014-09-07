package dyvil.tools.compiler.ast.statement;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class StatementList implements IValue
{
	private List<IValue>	statements	= new ArrayList();
	
	public List<IValue> getStatements()
	{
		return this.statements;
	}
	
	public void addStatement(IValue statement)
	{
		this.statements.add(statement);
	}
	
	public IValue getLastStatement()
	{
		return this.statements.get(this.statements.size() - 1);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public IValue fold()
	{
		return this;
	}

	@Override
	public Type getType()
	{
		return this.getLastStatement().getType();
	}
}
