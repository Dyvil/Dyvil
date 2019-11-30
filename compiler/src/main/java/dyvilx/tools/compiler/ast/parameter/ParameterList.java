package dyvilx.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.ResolvableList;
import dyvilx.tools.compiler.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ParameterList extends ArrayList<IParameter> implements ResolvableList<IParameter>
{
	// =============== Constructors ===============

	public ParameterList()
	{
	}

	public ParameterList(int capacity)
	{
		super(capacity);
	}

	public ParameterList(IParameter... parameters)
	{
		super(Arrays.asList(parameters));
	}

	// =============== Properties ===============

	public int explicitSize()
	{
		int count = 0;
		for (final IParameter param : this)
		{
			if (!param.isImplicit())
			{
				count++;
			}
		}
		return count;
	}

	public IParameter[] getParameters()
	{
		return this.toArray(new IParameter[0]);
	}

	// =============== Methods ===============

	// --------------- Modification ---------------

	public IParameter removeFirst()
	{
		return this.remove(0);
	}

	public IParameter removeLast()
	{
		return this.remove(this.size() - 1);
	}

	// --------------- Resolution ---------------

	public IParameter get(Name name)
	{
		for (IParameter param : this)
		{
			if (param.getName() == name)
			{
				return param;
			}
		}

		return null;
	}

	public boolean isParameter(IVariable variable)
	{
		return this.contains(variable);
	}

	public boolean matches(ParameterList other)
	{
		final int len = other.size();
		if (len != this.size())
		{
			return false;
		}

		for (int i = 0; i < len; i++)
		{
			final IType thisParameterType = this.get(i).getType();
			final IType otherParameterType = other.get(i).getType();
			if (!Types.isSameType(thisParameterType, otherParameterType))
			{
				return false;
			}
		}

		return true;
	}

	// --------------- Varargs ---------------

	public boolean isVariadic()
	{
		for (IParameter param : this)
		{
			if (param.isVarargs())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isLastVariadic()
	{
		return !this.isEmpty() && this.get(this.size() - 1).isVarargs();
	}

	// --------------- Descriptor and Signature ---------------

	public void appendDescriptor(StringBuilder builder)
	{
		for (IParameter param : this)
		{
			param.getInternalType().appendExtendedName(builder);
		}
	}

	public boolean needsSignature()
	{
		for (IParameter param : this)
		{
			if (param.hasModifier(Modifiers.SYNTHETIC))
			{
				return true;
			}

			final IType parameterType = param.getType();
			if (parameterType.isGenericType() || parameterType.hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	public void appendSignature(StringBuilder builder)
	{
		for (IParameter param : this)
		{
			if (!param.hasModifier(Modifiers.SYNTHETIC))
			{
				param.getInternalType().appendSignature(builder, false);
			}
		}
	}

	// --------------- Compilation ---------------

	public void writeLocals(MethodWriter writer, Label start, Label end)
	{
		for (IParameter param : this)
		{
			param.writeLocal(writer, start, end);
		}
	}

	public void write(MethodWriter writer)
	{
		for (IParameter param : this)
		{
			param.writeParameter(writer);
		}
		for (IParameter param : this)
		{
			param.writeInit(writer);
		}
	}

	// --------------- Serialization ---------------

	public void write(DataOutput out) throws IOException
	{
		final int size = this.size();
		out.writeByte(size);

		//noinspection ForLoopReplaceableByForEach to avoid concurrency problems
		for (int i = 0; i < size; i++)
		{
			this.get(i).write(out);
		}
	}

	public void writeSignature(DataOutput out) throws IOException
	{
		final int size = this.size();
		out.writeByte(size);

		//noinspection ForLoopReplaceableByForEach to avoid concurrency problems
		for (int i = 0; i < size; i++)
		{
			final IParameter param = this.get(i);
			param.getName().write(out);
			IType.writeType(param.getType(), out);
		}
	}

	public static ParameterList read(DataInput in) throws IOException
	{
		final int parameterCount = in.readByte();
		final ParameterList parameterList = new ParameterList(parameterCount);

		for (int i = 0; i < parameterCount; i++)
		{
			final CodeParameter param = new CodeParameter();
			param.read(in);
			parameterList.add(param);
		}

		return parameterList;
	}

	public void readSignature(DataInput in) throws IOException
	{
		final int parameterCount = in.readByte();

		this.clear();
		this.ensureCapacity(parameterCount);
		for (int i = 0; i < parameterCount; i++)
		{
			this.add(new CodeParameter(Name.read(in), IType.readType(in)));
		}
	}

	// --------------- Formatting ---------------

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

			if (!this.isEmpty())
			{
				Formatting.appendSeparator(buffer, "parameters.separator", ',');
			}
		}

		Util.astToString(indent, this.getParameters(), this.size(),
		                 Formatting.getSeparator("parameters.separator", ','), buffer);
		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	// --------------- Signature Formatting ---------------

	public void signatureToString(StringBuilder buffer, ITypeContext typeContext)
	{
		buffer.append('(');

		final int size = this.size();
		if (size > 0)
		{
			signatureToString(buffer, typeContext, this.get(0));
			for (int i = 1; i < size; i++)
			{
				buffer.append(", ");
				signatureToString(buffer, typeContext, this.get(i));
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
}
