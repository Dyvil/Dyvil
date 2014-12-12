package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;

public class WhileStatement extends ASTNode implements IStatement, IValued
{
	private IValue	condition;
	private IValue	then;
	
	public WhileStatement()
	{
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
	
	public IValue getThen()
	{
		return this.then;
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
	public WhileStatement applyState(CompilerState state, IContext context)
	{
		this.condition = this.condition.applyState(state, null);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.whileStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.whileEnd);
		
		if (this.then != null)
		{
			buffer.append(' ');
			this.then.toString(prefix, buffer);
		}
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
}
