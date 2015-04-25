package dyvil.tools.compiler.ast.expression;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class EncapsulatedValue implements IValue, IValued
{
	private IValue	value;
	
	public EncapsulatedValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public int valueTag()
	{
		return CAPSULATED;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.value.getPosition();
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.value.isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		return this.value.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		IValue value1 = this.value.withType(type);
		if (value1 == null)
		{
			return null;
		}
		this.value = value1;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.value.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
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
	}
	
	@Override
	public IValue foldConstants()
	{
		return this.value.foldConstants();
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.value.writeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.value.writeStatement(writer);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		this.value.writeJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		this.value.writeInvJump(writer, dest);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		this.value.toString(prefix, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}
