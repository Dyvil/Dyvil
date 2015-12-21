package dyvil.tools.compiler.transform;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class CaptureHelper
{
	private CaptureVariable[] capturedFields;
	private int               capturedFieldCount;
	private IClass            thisClass;

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
			this.capturedFields = new CaptureVariable[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureVariable(variable);
		}

		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable var = this.capturedFields[i];
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
			CaptureVariable[] temp = new CaptureVariable[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureVariable(variable);
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
			this.capturedFields[i].getActualType().appendExtendedName(buffer);
		}
	}

	public void writeCaptures(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable var = this.capturedFields[i];
			writer.writeVarInsn(var.getActualType().getLoadOpcode(), var.getVariable().getLocalIndex());
		}
	}

	public int writeCaptureParameters(MethodWriter writer)
	{
		int index = 0;
		if (this.thisClass != null)
		{
			writer.setThisType(this.thisClass.getInternalName());
			index = 1;
		}

		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable capture = this.capturedFields[i];
			capture.setLocalIndex(index);
			index = writer.registerParameter(index, capture.getName().qualified, capture.getActualType(), 0);
		}

		return index;
	}
}
