package dyvil.tools.compiler.transform;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.CaptureDataMember;
import dyvil.tools.compiler.ast.field.CaptureField;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.function.Function;

public class CaptureHelper
{
	private CaptureDataMember[] capturedFields;
	private int                 capturedFieldCount;

	private       IClass                                                   thisClass;
	private final Function<? super IVariable, ? extends CaptureDataMember> captureSupplier;

	public CaptureHelper(Function<? super IVariable, ? extends CaptureDataMember> captureSupplier)
	{
		this.captureSupplier = captureSupplier;
	}

	public void setThisClass(IClass thisClass)
	{
		this.thisClass = thisClass;
	}

	public IClass getThisClass()
	{
		return this.thisClass;
	}
	
	public IDataMember capture(IVariable variable)
	{
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureDataMember[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = this.captureSupplier.apply(variable);
		}

		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureDataMember var = this.capturedFields[i];
			if (var.getVariable() == variable)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return variable;
			}
		}

		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureDataMember[] temp = new CaptureDataMember[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = this.captureSupplier.apply(variable);
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

	public void writeCaptures(MethodWriter writer) throws BytecodeException
	{
		if (this.thisClass != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, 0);
		}

		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureDataMember capture = this.capturedFields[i];
			capture.getVariable().writeGet_Get(writer, 0);
		}
	}

	public int writeCaptureParameters(MethodWriter writer, int index)
	{
		if (this.thisClass != null)
		{
			writer.setThisType(this.thisClass.getInternalName());
			index = 1;
		}

		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			final CaptureDataMember capture = this.capturedFields[i];
			capture.setLocalIndex(index);
			index = writer
					.registerParameter(index, capture.getName().qualified, capture.getVariable().getInternalType(), 0);
		}

		return index;
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
			writer.writeVarInsn(Opcodes.ALOAD, 0);
			writer.writeVarInsn(field.getInternalType().getLoadOpcode(), field.getLocalIndex());
			writer.writeFieldInsn(Opcodes.PUTFIELD, field.enclosingClass.getInternalName(), field.name,
			                      field.getInternalType().getExtendedName());
		}
	}
}
