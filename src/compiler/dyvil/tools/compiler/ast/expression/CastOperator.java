package dyvil.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;

public final class CastOperator extends AbstractValue
{
	protected IValue value;
	protected IType  type;
	
	// Metadata
	private boolean typeHint;
	
	public CastOperator(SourcePosition position, IValue value)
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
	public boolean isResolved()
	{
		return this.type.isResolved();
	}

	@Override
	public boolean isPartialWildcard()
	{
		return this.value.isPartialWildcard();
	}

	@Override
	public IValue withLambdaParameter(IParameter parameter)
	{
		if (!this.isPartialWildcard())
		{
			return null;
		}

		parameter.setType(this.type);
		return this.value.withLambdaParameter(parameter);
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
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		IType valueType = this.value.getType();
		
		final IValue typedValue = TypeChecker.convertValue(this.value, this.type, this.type, markers, context);
		if (typedValue != null)
		{
			this.value = typedValue;
			
			final IType newType = typedValue.getType();
			if (!Types.isExactType(valueType, newType) && Types.isSuperClass(this.type, newType)
					&& newType.isPrimitive() == this.type.isPrimitive())
			{
				this.typeHint = true;
				this.type = newType;
				return this;
			}
			
			valueType = newType;
		}
		
		final boolean primitiveType = this.type.isPrimitive();
		final boolean primitiveValue = valueType.isPrimitive();
		
		if (typedValue == null && !(primitiveType && primitiveValue) && !Types.isSuperClass(valueType, this.type))
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
		this.type.checkType(markers, context, TypePosition.RETURN_TYPE);
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
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.typeHint)
		{
			return this.value.cleanup(compilableList, classCompilableList);
		}
		
		this.type.cleanup(compilableList, classCompilableList);
		this.value = this.value.cleanup(compilableList, classCompilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, null);

		if (type == null)
		{
			type = this.type;
		}

		if (Types.isVoid(type))
		{
			writer.visitInsn(Opcodes.AUTO_POP);
			return;
		}
		this.value.getType().writeCast(writer, type, this.lineNumber());
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" as ");
		this.type.toString(prefix, buffer);
	}
}
