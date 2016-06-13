package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ParameterList implements IParameterList
{
	protected IParameter[] parameters;
	protected int          parameterCount;

	public ParameterList()
	{
		this.parameters = new IParameter[3];
	}

	public ParameterList(int capacity)
	{
		this.parameters = new IParameter[capacity];
	}

	public ParameterList(IParameter parameter)
	{
		this.parameters = new IParameter[] { parameter };
		this.parameterCount = 1;
	}

	public ParameterList(IParameter... parameters)
	{
		this.parameters = parameters;
		this.parameterCount = parameters.length;
	}

	public ParameterList(IParameter[] parameters, int parameterCount)
	{
		this.parameters = parameters;
		this.parameterCount = parameterCount;
	}

	@Override
	public int size()
	{
		return this.parameterCount;
	}

	@Override
	public IParameter get(int index)
	{
		return this.parameters[index];
	}

	@Override
	public IParameter[] getParameterArray()
	{
		return this.parameters;
	}

	@Override
	public void set(int index, IParameter parameter)
	{
		parameter.setIndex(index);
		this.parameters[index] = parameter;
	}

	@Override
	public void setParameterArray(IParameter[] parameters, int parameterCount)
	{
		this.parameters = parameters;
		this.parameterCount = parameterCount;
	}

	@Override
	public void addParameter(IParameter parameter)
	{
		final int index = this.parameterCount++;

		parameter.setIndex(index);

		if (index >= this.parameters.length)
		{
			IParameter[] temp = new IParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = parameter;
	}

	public void remove(int count)
	{
		final int end = this.parameterCount;

		// Set excessive array elements to null to let the GC do it's job
		this.parameterCount -= count;
		for (int i = this.parameterCount; i < end; i++)
		{
			this.parameters[i] = null;
		}
	}

	// Resolution

	@Override
	public IParameter resolveParameter(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}

		return null;
	}

	@Override
	public boolean isParameter(IVariable variable)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameters[i] == variable)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean matches(IParameterList other)
	{
		final int len = other.size();
		if (len != this.parameterCount)
		{
			return false;
		}

		for (int i = 0; i < len; i++)
		{
			final IType thisParameterType = this.parameters[i].getType();
			final IType otherParameterType = other.get(i).getType();
			if (!Types.isSameType(thisParameterType, otherParameterType))
			{
				return false;
			}
		}

		return true;
	}

	// Compiler Phases

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(context, compilableList);
		}
	}

	// Compilation

	@Override
	public void appendDescriptor(StringBuilder builder)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getInternalType().appendExtendedName(builder);
		}
	}

	@Override
	public boolean needsSignature()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			final IType parameterType = this.parameters[i].getInternalType();
			if (parameterType.isGenericType() || parameterType.hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void appendSignature(StringBuilder builder)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getInternalType().appendSignature(builder);
		}
	}

	@Override
	public void writeLocals(MethodWriter writer, Label start, Label end)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeLocal(writer, start, end);
		}
	}

	@Override
	public void writeInit(MethodWriter writer)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeInit(writer);
		}
	}

	// Serialization

	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(out);
		}
	}

	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType.writeType(this.parameters[i].getType(), out);
		}
	}

	public static ParameterList read(DataInput in) throws IOException
	{
		int parameterCount = in.readByte();
		final ParameterList parameterList = new ParameterList(parameterCount);

		for (int i = 0; i < parameterCount; i++)
		{
			CodeParameter param = new CodeParameter();
			param.read(in);
			parameterList.parameters[i] = param;
		}

		return parameterList;
	}

	public void readSignature(DataInput in) throws IOException
	{
		int parameterCount = in.readByte();
		if (this.parameterCount == parameterCount)
		{
			for (int i = 0; i < parameterCount; i++)
			{
				this.parameters[i].setType(IType.readType(in));
			}
			this.parameterCount = parameterCount;
			return;
		}

		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			this.parameters[i] = new CodeParameter(Name.fromRaw("par" + i), IType.readType(in));
		}
	}

	// Formatting

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');
		Util.astToString(prefix, this.parameters, this.parameterCount,
		                 Formatting.getSeparator("parameters.separator", ','), buffer);
		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	@Override
	public void signatureToString(StringBuilder buffer, ITypeContext typeContext)
	{
		buffer.append('(');

		if (this.parameterCount > 0)
		{
			Util.typeToString(this.parameters[0].getType(), typeContext, buffer);
			for (int i = 1; i < this.parameterCount; i++)
			{
				buffer.append(", ");
				Util.typeToString(this.parameters[i].getType(), typeContext, buffer);
			}
		}

		buffer.append(')');
	}
}
