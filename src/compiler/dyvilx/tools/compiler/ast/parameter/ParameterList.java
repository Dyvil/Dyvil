package dyvilx.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Modifiers;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

public class ParameterList implements Iterable<IParameter>, Resolvable
{
	private static final int DEFAULT_CAPACITY = 3;

	protected IParameter[] parameters;
	protected int          size;

	public ParameterList()
	{
		this.parameters = new IParameter[DEFAULT_CAPACITY];
	}

	public ParameterList(int capacity)
	{
		this.parameters = new IParameter[capacity];
	}

	public ParameterList(IParameter parameter)
	{
		this.parameters = new IParameter[] { parameter };
		this.size = 1;
	}

	public ParameterList(IParameter... parameters)
	{
		this.parameters = parameters;
		this.size = parameters.length;
	}

	public ParameterList(IParameter[] parameters, int size)
	{
		this.parameters = parameters;
		this.size = size;
	}

	// List

	public boolean isEmpty()
	{
		return this.size <= 0;
	}

	public int size()
	{
		return this.size;
	}

	@Override
	public Iterator<IParameter> iterator()
	{
		return new ArrayIterator<>(this.parameters, 0, this.size);
	}

	public IParameter get(int index)
	{
		return this.parameters[index];
	}

	public IParameter[] getParameters()
	{
		return this.parameters;
	}

	public void setParameters(IParameter[] parameters, int parameterCount)
	{
		this.parameters = parameters;
		this.size = parameterCount;
	}

	public void add(IParameter parameter)
	{
		final int index = this.size++;
		if (index >= this.parameters.length)
		{
			final IParameter[] temp = new IParameter[index + 1];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}

		parameter.setIndex(index);
		this.parameters[index] = parameter;
	}

	public void insert(int index, IParameter parameter)
	{
		final int newSize = this.size + 1;
		if (newSize >= this.parameters.length)
		{
			final IParameter[] temp = new IParameter[newSize];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			temp[index] = parameter;
			System.arraycopy(this.parameters, index, temp, index + 1, this.size - index);
			this.parameters = temp;
		}
		else
		{
			System.arraycopy(this.parameters, index, this.parameters, index + 1, this.size - index);
			this.parameters[index] = parameter;
		}
		this.size = newSize;

		// Update indices
		for (int i = 0; i < newSize; i++)
		{
			this.parameters[i].setIndex(i);
		}
	}

	public IParameter removeFirst()
	{
		final IParameter result = this.parameters[0];
		System.arraycopy(this.parameters, 1, this.parameters, 0, this.size - 1);
		this.parameters[this.size] = null;
		this.size--;
		return result;
	}

	public IParameter removeLast()
	{
		final int index = this.size - 1;
		final IParameter result = this.parameters[index];
		this.parameters[index] = null;
		this.size = index;
		return result;
	}

	// Resolution

	public IParameter get(Name name)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}

		return null;
	}

	public boolean isParameter(IVariable variable)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (this.parameters[i] == variable)
			{
				return true;
			}
		}
		return false;
	}

	public boolean matches(ParameterList other)
	{
		final int len = other.size();
		if (len != this.size)
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

	public boolean isVariadic()
	{
		for (int i = 0; i < this.size; i++)
		{
			if (this.parameters[i].isVarargs())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isLastVariadic()
	{
		return this.size > 0 && this.parameters[this.size - 1].isVarargs();
	}

	// Compiler Phases

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	public void appendDescriptor(StringBuilder builder)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].getInternalType().appendExtendedName(builder);
		}
	}

	public boolean needsSignature()
	{
		for (int i = 0; i < this.size; i++)
		{
			final IParameter parameter = this.parameters[i];
			if (parameter.hasModifier(Modifiers.SYNTHETIC))
			{
				return true;
			}

			final IType parameterType = parameter.getType();
			if (parameterType.isGenericType() || parameterType.hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	public void appendSignature(StringBuilder builder)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IParameter parameter = this.parameters[i];
			if (!parameter.hasModifier(Modifiers.SYNTHETIC))
			{
				parameter.getInternalType().appendSignature(builder, false);
			}
		}
	}

	public void writeLocals(MethodWriter writer, Label start, Label end)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].writeLocal(writer, start, end);
		}
	}

	public void write(MethodWriter writer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].writeParameter(writer);
		}
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].writeInit(writer);
		}
	}

	// Serialization

	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.size);
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].write(out);
		}
	}

	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeByte(this.size);
		for (int i = 0; i < this.size; i++)
		{
			this.parameters[i].getName().write(out);
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
		final int parameterCount = in.readByte();
		if (this.size == parameterCount)
		{
			for (int i = 0; i < parameterCount; i++)
			{
				final IParameter parameter = this.parameters[i];
				parameter.setName(Name.read(in));
				parameter.setType(IType.readType(in));
			}
			return;
		}

		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			this.parameters[i] = new CodeParameter(Name.read(in), IType.readType(in));
		}
	}

	// Formatting
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.toString(null, indent, buffer);
	}


	public void toString(IType thisType, @NonNull String indent, @NonNull StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		if (thisType != null)
		{
			buffer.append("this");
			Formatting.appendSeparator(buffer, "parameter.type_ascription", ':');
			thisType.toString(indent, buffer);

			if (this.size > 0)
			{
				Formatting.appendSeparator(buffer, "parameters.separator", ',');
			}
		}

		Util.astToString(indent, this.parameters, this.size, Formatting.getSeparator("parameters.separator", ','),
		                 buffer);
		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	public void signatureToString(StringBuilder buffer, ITypeContext typeContext)
	{
		buffer.append('(');

		if (this.size > 0)
		{
			IParameter parameter = this.parameters[0];

			signatureToString(buffer, typeContext, parameter);
			for (int i = 1; i < this.size; i++)
			{
				buffer.append(", ");
				parameter = this.parameters[i];
				signatureToString(buffer, typeContext, parameter);
			}
		}

		buffer.append(')');
	}

	private static void signatureToString(StringBuilder buffer, ITypeContext typeContext, IParameter parameter)
	{
		final Name name = parameter.getName();
		if (name != null)
		{
			buffer.append(name).append(": ");
		}
		Util.typeToString(parameter.getType(), typeContext, buffer);
	}

	// Copying

	public void copyTo(ParameterList other)
	{
		other.setParameters(this.getParameters(), this.size());
	}
}
