package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

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
		boolean firstName = false;
		for (int i = 0; i < this.size; i++)
		{
			final Name nameAt = this.keys[i];
			if (nameAt == null)
			{
				if (!firstName && i == index)
				{
					return i;
				}
				continue;
			}

			if (nameAt == name)
			{
				return i;
			}

			firstName = true;
		}
		return -1;
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
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param, IImplicitContext implicitContext)
	{
		final int argIndex = this.findIndex(argumentIndex, param.getName());
		if (argIndex < 0)
		{
			// No argument for parameter name

			return param.isVarargs() ? 0 : -1;
		}

		if (!param.isVarargs())
		{
			// Not a varargs parameter

			return ArgumentList.checkMatch(values, types, matchStartIndex + argIndex, this.values[argIndex], param.getInternalType(),
			                               implicitContext) ? 0 : -1;
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
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
