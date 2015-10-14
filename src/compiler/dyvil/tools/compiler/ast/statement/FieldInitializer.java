package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FieldInitializer implements IStatement
{
	protected Variable variable;
	
	public FieldInitializer(Variable variable)
	{
		this.variable = variable;
	}
	
	public Variable getVariable()
	{
		return this.variable;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.variable.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.variable.setPosition(position);
	}
	
	@Override
	public int valueTag()
	{
		return VARIABLE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.variable.getType().isPrimitive();
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.variable.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.variable.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.variable.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.variable.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.variable.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.variable.writeInit(writer);
	}
	
	@Override
	public String toString()
	{
		return this.variable.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
