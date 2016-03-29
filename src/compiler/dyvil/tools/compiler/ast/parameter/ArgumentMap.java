package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IContext;
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

public final class ArgumentMap implements IArguments
{
	private Name[]   keys;
	private IValue[] values;
	private int      size;

	public ArgumentMap()
	{
		this.keys = new Name[3];
		this.values = new IValue[3];
	}

	public ArgumentMap(Name[] keys, IValue[] values, int size)
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
	public IArguments dropFirstValue()
	{
		return this; // FIXME
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

		System.arraycopy(this.keys, 0, keys, 0, size);
		System.arraycopy(this.values, 0, values, 0, size);
		keys[index] = name;
		values[index] = value;

		return new ArgumentMap(keys, values, size);
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
	public void setValue(int index, IParameter param, IValue value)
	{
		if (param == null)
		{
			this.values[index] = value;
			return;
		}

		Name key = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.keys[i] == key)
			{
				this.values[i] = value;
				return;
			}
		}
	}

	private boolean isNameAt(int index, int paramIndex, Name name)
	{
		final Name nameAt = this.keys[index];
		return nameAt == null ? index == paramIndex : nameAt == name;
	}

	@Override
	public IValue getValue(int index, IParameter param)
	{
		final Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.isNameAt(i, index, name))
			{
				return this.values[i];
			}
		}
		return null;
	}

	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		final IValue value = this.getValue(index, param);
		if (value != null)
		{
			return value.getTypeMatch(param.getInternalType());
		}
		return param.getValue() != null ? DEFAULT_MATCH : 0;
	}

	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		return this.getTypeMatch(index, param);
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (!this.isNameAt(i, index, name))
			{
				continue;
			}

			final IType type = param.getInternalType();
			this.values[i] = TypeChecker.convertValue(this.values[i], type, typeContext, markers, context,
			                                          IArguments.argumentMarkerSupplier(param));
			return;
		}
	}

	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.checkValue(index, param, typeContext, markers, context);
	}

	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		final Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.isNameAt(i, index, name))
			{
				param.getInternalType().inferTypes(this.values[i].getType(), typeContext);
				return;
			}
		}
	}

	@Override
	public void inferVarargsType(int index, IParameter param, ITypeContext typeContext)
	{
		this.inferType(index, param, typeContext);
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		final Name name = param.getName();
		for (int i = 0; i < this.size; i++)
		{
			if (this.isNameAt(i, index, name))
			{
				this.values[i].writeExpression(writer, param.getInternalType());
				return;
			}
		}

		param.getValue().writeExpression(writer, param.getInternalType());
	}

	@Override
	public void writeVarargsValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		this.writeValue(index, param, writer);
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
