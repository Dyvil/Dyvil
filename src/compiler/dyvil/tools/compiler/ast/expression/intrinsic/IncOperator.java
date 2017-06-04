package dyvil.tools.compiler.ast.expression.intrinsic;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;

public class IncOperator extends AbstractValue
{
	protected IValue      receiver;
	protected IDataMember field;

	private int     value;
	private boolean prefix;

	public IncOperator(IDataMember field, int value, boolean prefix)
	{
		this.field = field;
		this.value = value;
		this.prefix = prefix;
	}

	public IncOperator(IValue receiver, IDataMember field, int value, boolean prefix)
	{
		this.receiver = receiver;
		this.field = field;
		this.value = value;
		this.prefix = prefix;
	}

	public static IncOperator apply(IValue operand, int value, boolean prefix)
	{
		if (operand.valueTag() == IValue.FIELD_ACCESS && IncOperator.isIncConvertible(operand.getType()))
		{
			final FieldAccess fieldAccess = (FieldAccess) operand;

			return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), value, prefix);
		}
		return null;
	}

	private static boolean isIncConvertible(IType type)
	{
		if (!type.isPrimitive())
		{
			return false;
		}
		switch (type.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
		case PrimitiveType.VOID_CODE:
			return false;
		}
		return true;
	}

	@Override
	public int valueTag()
	{
		return INC;
	}

	@Override
	public boolean isUsableAsStatement()
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
		return this.field.getType();
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}

		if (this.field.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semanticError(this.position, "field.assign.final", this.field.getName()));
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int typecode = this.field.getType().getTypecode();
		final int lineNumber = this.lineNumber();
		boolean receiver = this.receiver != null;

		if (Types.isVoid(type))
		{
			// Prefix / Postfix doesn't make a difference here

			if (this.canUseIINC(typecode))
			{
				writer.visitIincInsn(((IVariable) this.field).getLocalIndex(), this.value);
				return;
			}

			this.field.writeSet_PreValue(writer, lineNumber);

			if (receiver)
			{
				this.receiver.writeExpression(writer, null);
				writer.visitInsn(Opcodes.DUP);
			}

			this.field.writeGet_Get(writer, lineNumber);
			this.field.writeGet_Unwrap(writer, lineNumber);
			this.writeAdd1(writer, typecode);

			this.field.writeSet_Wrap(writer, lineNumber);
			this.field.writeSet_Set(writer, lineNumber);
			return;
		}

		if (this.canUseIINC(typecode))
		{
			int localIndex = ((IVariable) this.field).getLocalIndex();
			if (this.prefix)
			{
				writer.visitIincInsn(localIndex, this.value);
				writer.visitVarInsn(this.field.getType().getLoadOpcode(), localIndex);
			}
			else
			{
				writer.visitVarInsn(this.field.getType().getLoadOpcode(), localIndex);
				writer.visitIincInsn(localIndex, this.value);
			}
		}
		else
		{
			int receiverIndex = 0;

			if (receiver)
			{
				receiverIndex = writer.localCount();

				this.receiver.writeExpression(writer, null);
				// Copy the receiver
				writer.visitInsn(Opcodes.DUP);
				// Store the receiver in a local variable
				writer.visitVarInsn(Opcodes.ASTORE, receiverIndex);
			}

			final boolean tempValue = this.field.writeSet_PreValue(writer, lineNumber);
			final int dupOpcode = tempValue || receiver ? Opcodes.AUTO_DUP_X1 : Opcodes.AUTO_DUP;

			// GETTER
			{
				if (receiver)
				{
					// Load the receiver again
					writer.visitVarInsn(Opcodes.ALOAD, receiverIndex);
				}

				// Load the old value
				this.field.writeGet_Get(writer, lineNumber);
				this.field.writeGet_Unwrap(writer, lineNumber);
			}

			if (this.prefix)
			{
				// Compute the new value
				this.writeAdd1(writer, typecode);
				// Copy the new value
				writer.visitInsn(dupOpcode);
			}
			else
			{
				// Copy the old value
				writer.visitInsn(dupOpcode);
				// Compute the new value
				this.writeAdd1(writer, typecode);
			}

			// Store the field
			this.field.writeSet_Wrap(writer, lineNumber);
			this.field.writeSet_Set(writer, lineNumber);
			// The new value is left on the stack

			if (receiver)
			{
				writer.resetLocals(receiverIndex);
			}
		}

		if (type != null)
		{
			this.field.getType().writeCast(writer, type, lineNumber);
		}
	}

	public boolean canUseIINC(int typecode) throws BytecodeException
	{
		if (this.receiver != null)
		{
			return false;
		}
		if (!this.field.isLocal())
		{
			return false;
		}
		if (((IVariable) this.field).isReferenceType())
		{
			return false;
		}

		switch (typecode)
		{
		case PrimitiveType.BYTE_CODE:
		case PrimitiveType.SHORT_CODE:
		case PrimitiveType.CHAR_CODE:
		case PrimitiveType.INT_CODE:
			return true;
		}
		return false;
	}

	public void writeAdd1(MethodWriter writer, int typecode) throws BytecodeException
	{
		switch (typecode)
		{
		case PrimitiveType.BYTE_CODE:
		case PrimitiveType.SHORT_CODE:
		case PrimitiveType.CHAR_CODE:
		case PrimitiveType.INT_CODE:
			writer.visitLdcInsn(this.value);
			writer.visitInsn(Opcodes.IADD);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitLdcInsn((long) this.value);
			writer.visitInsn(Opcodes.LADD);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitLdcInsn((float) this.value);
			writer.visitInsn(Opcodes.FADD);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer.visitLdcInsn((double) this.value);
			writer.visitInsn(Opcodes.DADD);
			return;
		default:
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.value == 1)
		{
			if (this.prefix)
			{
				buffer.append("++");
				this.appendAccess(indent, buffer);
			}
			else
			{
				this.appendAccess(indent, buffer);
				buffer.append("++");
			}
		}
		else if (this.value == -1)
		{
			if (this.prefix)
			{
				buffer.append("--");
				this.appendAccess(indent, buffer);
			}
			else
			{
				this.appendAccess(indent, buffer);
				buffer.append("--");
			}
		}
		else if (this.value > 0)
		{
			this.appendAccess(indent, buffer);
			buffer.append(" += ").append(this.value);
		}
		else if (this.value < 0)
		{
			this.appendAccess(indent, buffer);
			buffer.append(" -= ").append(-this.value);
		}
		else // this.value == 0
		{
			this.appendAccess(indent, buffer);
		}
	}

	private void appendAccess(@NonNull String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
			buffer.append('.');
		}

		buffer.append(this.field.getName());
	}
}
