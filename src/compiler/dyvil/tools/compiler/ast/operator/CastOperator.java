package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CastOperator extends AbstractValue
{
	protected IValue value;
	protected IType  type;
	
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
		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);
		}
		else
		{
			this.type = Types.UNKNOWN;
			markers.add(Markers.semanticError(this.position, "cast.type.invalid"));
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "cast.value.invalid"));
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);

		if (this.value == null)
		{
			return this;
		}

		this.value = this.value.resolve(markers, context);
		if (this.type == Types.VOID)
		{
			markers.add(Markers.semantic(this.position, "cast.void"));
			return this;
		}
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		IType valueType = this.value.getType();
		
		final IValue typedValue = this.value.withType(this.type, this.type, markers, context);
		if (typedValue != null)
		{
			this.value = typedValue;
			
			final IType newType = typedValue.getType();
			if (!valueType.isSameType(newType) && this.type.isSuperClassOf(newType)
					&& newType.isPrimitive() == this.type.isPrimitive())
			{
				this.typeHint = true;
				this.type = newType;
				return this;
			}
			
			valueType = newType;
		}
		
		final boolean primitiveType = this.type.isPrimitive();
		final boolean primitiveValue = this.value.isPrimitive();
		
		if (typedValue == null && !(primitiveType && primitiveValue) && !valueType.isSuperClassOf(this.type))
		{
			markers.add(Markers.semantic(this.position, "cast.incompatible", valueType, this.type));
			return this;
		}
		
		if (!this.typeHint && this.type.isSameType(valueType) && primitiveType == primitiveValue)
		{
			markers.add(Markers.semantic(this.position, "cast.unnecessary"));
			this.typeHint = true;
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		if (this.value != null)
		{
			this.value.check(markers, context);
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
