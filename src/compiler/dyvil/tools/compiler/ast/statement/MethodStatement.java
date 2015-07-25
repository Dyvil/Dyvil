package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.NestedMethod;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class MethodStatement implements IStatement
{
	private NestedMethod method;
	
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
	public int valueTag()
	{
		return NESTED_METHOD;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.resolveTypes(markers, context);
		this.method.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.resolve(markers, context);
		this.method.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.checkTypes(markers, context);
		this.method.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.check(markers, context);
		this.method.context = null;
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
		
		this.method.context = context;
		this.method.cleanup(context, compilableList);
		this.method.context = null;
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
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
