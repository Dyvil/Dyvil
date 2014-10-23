package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class DoWhileStatement extends ASTObject implements IStatement, IValued
{
	private IValue	then;
	private IValue		condition;
	
	public DoWhileStatement()
	{
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return this.then.getType();
	}
	
	@Override
	public DoWhileStatement applyState(CompilerState state, IContext context)
	{
		this.condition = this.condition.applyState(state, context);
		this.then = this.then.applyState(state, context);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}
