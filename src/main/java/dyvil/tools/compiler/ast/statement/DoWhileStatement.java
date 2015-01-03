package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;

public class DoWhileStatement extends ASTNode implements IStatement, IValued
{
	private IValue	then;
	private IValue	condition;
	
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
	public IType getType()
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
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
