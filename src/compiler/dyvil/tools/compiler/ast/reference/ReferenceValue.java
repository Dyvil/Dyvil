package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ReferenceValue implements IValue
{
	protected       IValue     value;
	protected final IReference reference;
	
	public ReferenceValue(IValue value, IReference reference)
	{
		this.value = value;
		this.reference = reference;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.value.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return REFERENCE;
	}
	
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public boolean isResolved()
	{
		return this.value.isResolved();
	}
	
	@Override
	public IType getType()
	{
		return this.value.getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.value = this.value.withType(type, typeContext, markers, context);
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.value.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return this.value.getTypeMatch(type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		
		this.reference.check(this.value.getPosition(), markers);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.value = this.value.cleanup(context, compilableList);
		this.reference.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.reference.writeReference(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
	}
}
