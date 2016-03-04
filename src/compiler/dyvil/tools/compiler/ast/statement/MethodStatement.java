package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.NestedMethod;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class MethodStatement implements IStatement
{
	protected NestedMethod method;
	
	public MethodStatement(NestedMethod method)
	{
		this.method = method;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.method.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.method.setPosition(position);
	}
	
	@Override
	public int valueTag()
	{
		return NESTED_METHOD;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.method.setEnclosingClass(context.getThisClass());
		this.method.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.method.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.method.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.method.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.method.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		compilableList.addCompilable(this.method);

		this.method.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.method.toString(prefix, buffer);
	}
}
