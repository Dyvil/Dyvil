package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Arrays;
import java.util.Iterator;

public final class NamedArgumentList implements IArguments
{
	private Name[]   keys;
	private IValue[] values;
	private int      size;

	public NamedArgumentList()
	{
		this.keys = new Name[3];
		this.values = new IValue[3];
	}

	public NamedArgumentList(Name[] keys, IValue[] values, int size)
	{
		this.keys = keys;
		this.values = values;
		this.size = size;
	}

	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, this.size);
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public IArguments withLastValue(IValue value)
	{
		return this.withLastValue(null, value);
	}

	@Override
	public IArguments withLastValue(Name name, IValue value)
	{
		int size = this.size;
		final int index = size++;
		final Name[] keys = new Name[size];
		final IValue[] values = new IValue[size];

		System.arraycopy(this.keys, 0, keys, 0, this.size);
		System.arraycopy(this.values, 0, values, 0, this.size);
		keys[index] = name;
		values[index] = value;

		return new NamedArgumentList(keys, values, size);
	}

	@Override
	public IValue getFirstValue()
	{
		return this.values[0];
	}

	public void addLastValue(Name key, IValue value)
	{
		final int index = this.size++;
		if (index >= this.values.length)
		{
			final Name[] tempKeys = new Name[this.size];
			final IValue[] tempValues = new IValue[this.size];
			System.arraycopy(this.keys, 0, tempKeys, 0, index);
			System.arraycopy(this.values, 0, tempValues, 0, index);
			this.keys = tempKeys;
			this.values = tempValues;
		}
		this.values[index] = value;
		this.keys[index] = key;
	}

	@Override
	public void setFirstValue(IValue value)
	{
		this.values[0] = value;
	}

	@Override
	public IValue getLastValue()
	{
		return this.values[this.size - 1];
	}

	@Override
	public void setLastValue(IValue value)
	{
		this.values[this.size - 1] = value;
	}

	@Override
	public IValue getValue(int index, IParameter param)
	{
		if (param == null)
		{
			return this.values[index];
		}

		final int argIndex = this.findIndex(index, param.getName());
		if (argIndex < 0)
		{
			return null;
		}
		return this.values[argIndex];
	}

	@Override
	public void setValue(int index, IParameter param, IValue value)
	{
		if (param == null)
		{
			this.values[index] = value;
			return;
		}

		final int argIndex = this.findIndex(index, param.getName());
		if (argIndex >= 0)
		{
			this.values[argIndex] = value;
		}
	}

	private int findIndex(int index, Name name)
	{
		// First, try to match the parameter name against the argument labels
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == name)
			{
				return i;
			}
		}

		// The specified name was not found, check the indices:

		if (index >= this.size)
		{
			return -1;
		}

		// Require that no argument labels exists before or at the index
		for (int i = 0; i <= index; i++)
		{
			if (this.keys[i] != null)
			{
				return -1;
			}
		}

		// No argument labels present before the requested index
		return index;
	}

	private int findNextName(int startIndex)
	{
		for (; startIndex < this.size; startIndex++)
		{
			if (this.keys[startIndex] != null)
			{
				return startIndex;
			}
		}

		return this.size;
	}

	@Override
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param,
		                     IImplicitContext implicitContext)
	{
		final int argIndex = this.findIndex(argumentIndex, param.getName());
		if (argIndex < 0)
		{
			// No argument for parameter name

			return param.isVarargs() ? 0 : -1;
		}
		if (this.keys[argIndex] == null && param.hasModifier(Modifiers.EXPLICIT))
		{
			return -1;
		}

		if (!param.isVarargs())
		{
			// Not a varargs parameter

			return ArgumentList.checkMatch(values, types, matchStartIndex + argIndex, this.values[argIndex],
			                               param.getInternalType(), implicitContext) ? 0 : -1;
		}

		// Varargs Parameter
		final int endIndex = this.findNextName(argIndex + 1);
		return ArgumentList.checkVarargsMatch(values, types, matchStartIndex, this.values, argIndex, endIndex, param,
		                                      implicitContext);
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final int argIndex = this.findIndex(index, param.getName());
		if (argIndex < 0)
		{
			return;
		}

		if (!param.isVarargs())
		{
			final IType type = param.getInternalType();
			this.values[argIndex] = TypeChecker.convertValue(this.values[argIndex], type, typeContext, markers, context,
			                                                 IArguments.argumentMarkerSupplier(param));
			return;
		}

		final int endIndex = this.findNextName(argIndex + 1);
		if (ArgumentList.checkVarargsValue(this.values, argIndex, endIndex, param, typeContext, markers, context))
		{
			final int moved = this.size - endIndex;
			if (moved > 0)
			{
				System.arraycopy(this.values, endIndex, this.values, argIndex + 1, moved);
				System.arraycopy(this.keys, endIndex, this.keys, argIndex + 1, moved);
			}
			this.size = argIndex + moved + 1;
		}
	}

	@Override
	public boolean hasParameterOrder()
	{
		return this.size <= 1;
	}

	@Override
	public void writeValues(MethodWriter writer, IParameterList parameters, int startIndex) throws BytecodeException
	{
		if (this.hasParameterOrder())
		{
			IArguments.super.writeValues(writer, parameters, startIndex);
			return;
		}

		final int locals = writer.localCount();

		final int paramCount = parameters.size() - startIndex;

		// Step 1: Associate parameters to arguments
		final IParameter[] params = new IParameter[this.size];
		for (int i = 0; i < paramCount; i++)
		{
			final IParameter param = parameters.get(i + startIndex);
			final int argIndex = this.findIndex(i, param.getName());
			if (argIndex >= 0)
			{
				params[argIndex] = param;
			}
		}

		// Save the local indices in the targets array for later use
		// Maps parameter index -> local index of stored argument
		final int[] targets = new int[paramCount];
		// Fill the array with -1s to mark missing values
		Arrays.fill(targets, -1);

		// Step 2: Write all arguments that already have parameter order
		int argStartIndex = 0;
		for (int i = 0; i < this.size; i++)
		{
			IParameter param = params[i];
			if (param.getIndex() == startIndex + i)
			{
				this.values[i].writeExpression(writer, param.getInternalType());
				targets[i] = -2;
				argStartIndex = i + 1;
			}
			else
			{
				break;
			}
		}

		// Step 3: Write the remaining arguments in order and save them in local variables

		if (argStartIndex < this.size)
		{
			for (int i = argStartIndex; i < this.size; i++)
			{
				final IParameter parameter = params[i];
				final IType parameterType = parameter.getInternalType();
				final IValue value = this.values[i];

				final int localIndex = writer.localCount();
				writer.setLocalType(localIndex, parameterType.getFrameType());

				targets[parameter.getIndex() - startIndex] = localIndex;

				value.writeExpression(writer, parameterType);
				writer.visitVarInsn(parameterType.getStoreOpcode(), localIndex);
			}

			// Step 4: Load the local variables in order
			for (int i = 0; i < paramCount; i++)
			{
				final IParameter parameter = parameters.get(i + startIndex);
				final int target = targets[parameter.getIndex() - startIndex];

				switch (target)
				{
				case -1:
					// Value for parameter does not exist -> write default value
					EmptyArguments.writeArguments(writer, parameter);
					continue;
				case -2:
					// Value for parameter was already written in Step 2
					continue;
				default:
					// Value for parameter exists -> load the variable
					writer.visitVarInsn(parameter.getInternalType().getLoadOpcode(), target);
				}
			}
		}

		writer.resetLocals(locals);
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		final int argIndex = this.findIndex(index, param.getName());
		if (argIndex >= 0)
		{
			this.values[argIndex].writeExpression(writer, param.getInternalType());
			return;
		}

		EmptyArguments.writeArguments(writer, param);
	}

	@Override
	public boolean isResolved()
	{
		for (int i = 0; i < this.size; i++)
		{
			if (!this.values[i].isResolved())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			final Name key = this.keys[i];
			final IValue value = this.values[i];

			value.resolveTypes(markers, context);

			if (key == null)
			{
				continue;
			}

			for (int j = 0; j < i; j++)
			{
				if (this.keys[j] == key)
				{
					markers.add(Markers.semanticError(value.getPosition(), "arguments.duplicate.key", key));
					break;
				}
			}
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public IArguments copy()
	{
		return new NamedArgumentList(Arrays.copyOf(this.keys, this.size), Arrays.copyOf(this.values, this.size),
		                             this.size);
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		if (this.size > 0)
		{
			for (int i = 0, len = this.size; ; i++)
			{
				final Name key = this.keys[i];
				if (key != null)
				{
					buffer.append(key);
					Formatting.appendSeparator(buffer, "parameters.name_value_separator", ':');
				}

				this.values[i].toString(prefix, buffer);
				if (i + 1 >= len)
				{
					break;
				}

				Formatting.appendSeparator(buffer, "parameters.separator", ',');
			}
		}

		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		for (int i = 0, len = this.size; ; i++)
		{
			final Name key = this.keys[i];

			if (key != null)
			{
				buffer.append(key).append(": ");
			}

			this.values[i].getType().toString("", buffer);

			if (i + 1 >= len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}
}
