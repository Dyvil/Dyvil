package dyvilx.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class InstanceOfOperator extends AbstractValue
{
	protected IValue value;
	protected IType  type;
	
	public InstanceOfOperator(SourcePosition position, IValue value)
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "instanceof.type.invalid"));
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "instanceof.value.invalid"));
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.resolve(markers, context);
		}
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.checkType(markers, context, TypePosition.CLASS);
		}
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.check(markers, context);

			if (this.type.isPrimitive())
			{
				markers.add(Markers.semanticError(this.position, "instanceof.type.primitive"));
				return;
			}
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "instanceof.type.primitive"));
			return;
		}

		if (this.value != null)
		{
			this.value.check(markers, context);
		}
		else
		{
			return;
		}
		
		final IType valueType = this.value.getType();
		if (valueType.isPrimitive())
		{
			markers.add(Markers.semanticError(this.position, "instanceof.value.primitive"));
			return;
		}
		if (Types.isExactType(this.type, valueType))
		{
			markers.add(Markers.semantic(this.position, "instanceof.type.equal", valueType));
			return;
		}
		if (Types.isSuperType(this.type, valueType))
		{
			markers.add(Markers.semantic(this.position, "instanceof.type.subtype", valueType, this.type));
			return;
		}
		if (!Types.isSuperType(valueType, this.type))
		{
			markers.add(Markers.semanticError(this.position, "instanceof.type.incompatible", valueType, this.type));
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
		this.type.cleanup(compilableList, classCompilableList);
		this.value = this.value.cleanup(compilableList, classCompilableList);
		
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
		writer.visitTypeInsn(Opcodes.INSTANCEOF, this.type.getInternalName());

		if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.lineNumber());
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
