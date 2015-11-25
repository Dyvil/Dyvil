package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class InstanceOfOperator extends AbstractValue
{
	protected IValue	value;
	protected IType		type;
	
	public InstanceOfOperator(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	public InstanceOfOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int valueTag()
	{
		return ISOF_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN) ? this : null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		this.value.check(markers, context);
		
		if (this.type.isPrimitive())
		{
			markers.add(I18n.createError(this.position, "instanceof.type.primitive"));
			return;
		}
		if (this.value.isPrimitive())
		{
			markers.add(I18n.createError(this.position, "instanceof.value.primitive"));
			return;
		}
		
		IType valueType = this.value.getType();
		if (valueType.classEquals(this.type))
		{
			markers.add(I18n.createMarker(this.position, "instanceof.type.equal", valueType));
			return;
		}
		if (this.type.isSuperClassOf(valueType))
		{
			markers.add(I18n.createMarker(this.position, "instanceof.type.subtype", valueType, this.type));
			return;
		}
		if (!valueType.isSuperClassOf(this.type))
		{
			markers.add(I18n.createError(this.position, "instanceof.type.incompatible", valueType, this.type));
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		this.value = this.value.cleanup(context, compilableList);
		
		if (this.value.isType(this.type))
		{
			return BooleanValue.TRUE;
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		writer.writeTypeInsn(Opcodes.INSTANCEOF, this.type.getInternalName());

		if (type == Types.VOID)
		{
			writer.writeInsn(Opcodes.IRETURN);
		}
		else if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" is ");
		this.type.toString(prefix, buffer);
	}
}
