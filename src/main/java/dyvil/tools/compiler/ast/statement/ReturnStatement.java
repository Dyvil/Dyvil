package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class ReturnStatement extends ASTObject implements IStatement, IValued
{
	protected IValue	value;
	
	public ReturnStatement()
	{}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return this.value.getType();
	}
	
	@Override
	public IValue applyState(CompilerState state)
	{
		this.value = this.value.applyState(state);
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			return this.value;
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("return ");
		this.value.toString("", buffer);
	}
}
