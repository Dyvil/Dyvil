package dyvil.tools.compiler.ast.boxed;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.Util;

public class BoxedValue extends ASTNode implements IValue
{
	public IValue	boxed;
	public IMethod	boxingMethod;
	
	public BoxedValue(IValue boxed, IMethod boxingMethod)
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return null;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
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
		this.boxingMethod.writeCall(writer, this.boxed, Util.EMPTY_VALUES);
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
