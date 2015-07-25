package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class BoxedValue implements IValue
{
	private IValue	boxed;
	private IMethod	method;
	
	public BoxedValue(IValue boxed, IMethod boxingMethod)
	{
		this.boxed = boxed;
		this.method = boxingMethod;
	}
	
	@Override
	public int valueTag()
	{
		return BOXED;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.boxed.getPosition();
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.method.getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.boxed.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.boxed = this.boxed.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.boxed.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.boxed.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.boxed = this.boxed.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.boxed = this.boxed.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, this.boxed, EmptyArguments.INSTANCE, null, this.boxed.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.boxed.writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.boxed.toString(prefix, buffer);
	}
}
