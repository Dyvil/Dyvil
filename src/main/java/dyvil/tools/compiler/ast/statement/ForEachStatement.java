package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class ForEachStatement extends ASTObject implements IStatement
{
	private Field	variable;
	private IValue	iterable;
	
	public ForEachStatement()
	{}
	
	public void setVariable(Field variable)
	{
		this.variable = variable;
	}
	
	public void setIterable(IValue iterable)
	{
		this.iterable = iterable;
	}
	
	public Field getVariable()
	{
		return this.variable;
	}
	
	public IValue getIterable()
	{
		return this.iterable;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		// TODO
		return null;
	}
	
	@Override
	public ForEachStatement applyState(CompilerState state, IContext context)
	{
		this.variable = this.variable.applyState(state, context);
		this.iterable = this.iterable.applyState(state, context);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		// TODO
	}
}
