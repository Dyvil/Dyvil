package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class BoxValue implements IValue
{
	public IValue	boxed;
	public IMethod	boxingMethod;
	
	public BoxValue(IValue boxed, IMethod boxingMethod)
	{
		this.boxed = boxed;
		this.boxingMethod = boxingMethod;
	}
	
	@Override
	public int getValueType()
	{
		return BOXED;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.boxed.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this.boxed.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.boxed.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.boxed.getTypeMatch(type);
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
		this.boxed.checkTypes(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.boxed = this.boxed.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.boxingMethod.writeCall(writer, this.boxed, EmptyArguments.INSTANCE);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.boxed.writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.boxed.toString(prefix, buffer);
	}
}
