package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CastOperator extends AbstractValue
{
	protected IValue value;
	protected IType type = Types.UNKNOWN;
	
	// Metadata
	private boolean typeHint;
	
	public CastOperator(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	public CastOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int valueTag()
	{
		return CAST_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.type.isPrimitive();
	}
	
	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type.isSuperTypeOf(this.type) ? this : null;
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
		if (this.type == Types.VOID)
		{
			markers.add(MarkerMessages.createMarker(this.position, "cast.void"));
			return this;
		}
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		IType prevType = this.value.getType();
		
		IValue value1 = this.value.withType(this.type, this.type, markers, context);
		if (value1 != null)
		{
			this.value = value1;
			
			IType valueType = value1.getType();
			if (!prevType.isSameType(valueType) && this.type.isSuperClassOf(valueType)
					&& valueType.isPrimitive() == this.type.isPrimitive())
			{
				this.typeHint = true;
				this.type = valueType;
				return this;
			}
			
			prevType = valueType;
		}
		
		boolean primitiveType = this.type.isPrimitive();
		boolean primitiveValue = this.value.isPrimitive();
		
		if (value1 == null && !(primitiveType && primitiveValue) && !prevType.isSuperClassOf(this.type))
		{
			markers.add(MarkerMessages.createMarker(this.position, "cast.incompatible", prevType, this.type));
			return this;
		}
		
		if (!this.typeHint && this.type.isSameType(prevType) && primitiveType == primitiveValue)
		{
			markers.add(MarkerMessages.createMarker(this.position, "cast.unnecessary"));
			this.typeHint = true;
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		this.value.check(markers, context);
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
		if (this.typeHint)
		{
			return this.value.cleanup(context, compilableList);
		}
		
		this.type.cleanup(context, compilableList);
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, null);

		if (type == Types.VOID)
		{
			writer.writeInsn(Opcodes.AUTO_POP);
			return;
		}

		if (type == null)
		{
			type = this.type;
		}
		this.value.getType().writeCast(writer, type, this.getLineNumber());
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" as ");
		this.type.toString(prefix, buffer);
	}
}
