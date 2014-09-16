package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class ForEachStatement extends ASTObject implements IStatement
{
	private Field variable;
	private Object iterable;
	
	public ForEachStatement()
	{
	}
	
	public void setVariable(Field variable)
	{
		this.variable = variable;
	}
	
	public void setIterable(Object iterable)
	{
		this.iterable = iterable;
	}
	
	public Field getVariable()
	{
		return this.variable;
	}
	
	public Object getIterable()
	{
		return this.iterable;
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
		// TODO
		return null;
	}
	
	@Override
	public void applyState(CompilerState state)
	{}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		//TODO
	}
}
