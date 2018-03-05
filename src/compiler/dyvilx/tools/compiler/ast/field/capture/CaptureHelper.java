package dyvilx.tools.compiler.ast.field.capture;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Iterator;
import java.util.function.Function;

public class CaptureHelper<T extends CaptureDataMember> implements Iterable<T>
{
	private final Function<? super IVariable, ? extends T> captureSupplier;

	private T[]    capturedFields;
	private int    capturedFieldCount;
	private IClass thisClass;

	public CaptureHelper(Function<? super IVariable, ? extends T> captureSupplier)
	{
		this.captureSupplier = captureSupplier;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<>(this.capturedFields, this.capturedFieldCount);
	}

	public IClass getThisClass()
	{
		return this.thisClass;
	}

	public void setThisClass(IClass thisClass)
	{
		this.thisClass = thisClass;
	}

	public T capture(IVariable variable)
	{
		if (this.capturedFields == null)
		{
			this.capturedFields = (T[]) new CaptureDataMember[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = this.captureSupplier.apply(variable);
		}

		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final T capture = this.capturedFields[i];
			if (capture.getVariable() == variable)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return capture;
			}
		}

		final int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			final T[] temp = (T[]) new CaptureDataMember[index + 1];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = this.captureSupplier.apply(variable);
	}

	public boolean isMember(IVariable variable)
	{
		if (this.capturedFields == null)
		{
			return false;
		}
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			if (this.capturedFields[i] == variable)
			{
				return true;
			}
		}
		return false;
	}

	public void checkCaptures(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			// Check captures
			this.capturedFields[i].checkTypes(markers, context);
		}
	}

	public boolean hasCaptures()
	{
		return this.capturedFieldCount > 0;
	}

	public boolean isThisCaptured()
	{
		return this.thisClass != null;
	}

	public void appendThisCaptureType(StringBuilder buffer)
	{
		if (this.thisClass != null)
		{
			buffer.append('L').append(this.thisClass.getInternalName()).append(';');
		}
	}

	public void appendCaptureTypes(StringBuilder buffer)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].getVariable().getInternalType().appendExtendedName(buffer);
		}
	}

	public void writeCaptures(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.thisClass != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, 0);
		}

		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureDataMember capture = this.capturedFields[i];
			// variables typically don't have receivers
			capture.getVariable().writeGetRaw(writer, null, lineNumber);
		}
	}

	public void writeCaptureParameters(MethodWriter writer, int index)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureDataMember capture = this.capturedFields[i];
			capture.setLocalIndex(index);
			index = writer.visitParameter(index, capture.getInternalName(), capture.getVariable().getInternalType(), 0);
		}
	}

	public void writeCaptureFields(ClassWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureField field = (CaptureField) this.capturedFields[i];
			field.write(writer);
		}
	}

	public void writeFieldAssignments(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureField field = (CaptureField) this.capturedFields[i];
			writer.visitVarInsn(Opcodes.ALOAD, 0);
			writer.visitVarInsn(field.getInternalType().getLoadOpcode(), field.getLocalIndex());
			writer.visitFieldInsn(Opcodes.PUTFIELD, field.enclosingClass.getInternalName(), field.getInternalName(),
			                      field.getDescriptor());
		}
	}
}
