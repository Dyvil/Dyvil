package dyvil.tools.compiler.ast.statement;

import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ReturnStatement extends AbstractValue implements IValueConsumer
{
	protected IValue value;
	
	public ReturnStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public ReturnStatement(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int valueTag()
	{
		return RETURN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.value.isPrimitive();
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return this.value == null || this.value.isResolved();
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
		return this.value == null ? Types.VOID : this.value.getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSameType(type, Types.VOID))
		{
			return this;
		}
		if (this.value == null)
		{
			return null;
		}
		IValue value1 = this.value.withType(type, typeContext, markers, context);
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
		return Types.isSameType(type, Types.VOID) || this.value != null && this.value.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.value == null)
		{
			return 0;
		}
		
		return this.value.getTypeMatch(type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		final IType returnType = context.getReturnType();

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);

			if (returnType == null) {
				return;
			}

			final IType valueType = this.value.getType();
			if (!Types.isSuperType(returnType, valueType))
			{
				final Marker marker = Markers.semanticError(this.position, "return.type.incompatible");
				marker.addInfo(Markers.getSemantic("return.type", returnType));
				marker.addInfo(Markers.getSemantic("value.type", valueType));
				markers.add(marker);
			}
		}
		else if (returnType != null && returnType != Types.VOID)
		{
			markers.add(Markers.semanticError(this.position, "return.void.invalid"));
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.value == null)
		{
			if (type == null || Types.isSameType(type, Types.VOID))
			{
				writer.visitInsn(Opcodes.RETURN);
				return;
			}

			type.writeDefaultValue(writer);
			return;
		}

		if (Types.isSameType(type, Types.VOID))
		{
			this.value.writeExpression(writer, null);
			writer.visitInsn(this.value.getType().getReturnOpcode());
			return;
		}

		this.value.writeExpression(writer, type);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value != null)
		{
			buffer.append("return ");
			this.value.toString("", buffer);
		}
		else
		{
			buffer.append("return");
		}
	}
}
