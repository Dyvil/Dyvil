package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ThrowStatement extends Value implements IValueConsumer
{
	protected IValue value;
	
	public ThrowStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public ThrowStatement(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int valueTag()
	{
		return THROW;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return 1;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IValue value1 = this.value.withType(Types.THROWABLE, null, markers, context);
		if (value1 == null)
		{
			Marker marker = I18n.createMarker(this.value.getPosition(), "throw.type");
			marker.addInfo(I18n.getString("value.type", this.value.getType()));
			markers.add(marker);
		}
		else
		{
			this.value = value1;
		}
		
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		
		IType type = this.value.getType();
		if (Types.THROWABLE.isSuperTypeOf(type) && !Types.RUNTIME_EXCEPTION.isSuperTypeOf(type) && !context.handleException(this.value.getType()))
		{
			markers.add(I18n.createMarker(this.value.getPosition(), "method.access.exception", type.toString()));
		}
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
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.value.writeExpression(writer);
		writer.writeInsn(Opcodes.ATHROW);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("throw ");
		this.value.toString(prefix, buffer);
	}
}
