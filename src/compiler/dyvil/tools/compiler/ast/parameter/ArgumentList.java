package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public final class ArgumentList implements IArguments, IValueList
{
	private IValue[] values;
	private int      size;

	public ArgumentList()
	{
		this.values = new IValue[3];
	}

	public ArgumentList(IValue... values)
	{
		this.values = values;
		this.size = values.length;
	}

	public ArgumentList(int size)
	{
		this.values = new IValue[size];
	}

	public ArgumentList(IValue[] values, int size)
	{
		this.values = values;
		this.size = size;
	}

	public IValue[] getValues()
	{
		return this.values;
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
	public int valueCount()
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
		int len1 = this.size - 1;
		IValue[] values = new IValue[len1];
		System.arraycopy(this.values, 1, values, 0, len1);
		return new ArgumentList(values, len1);
	}

	@Override
	public IArguments withLastValue(IValue value)
	{
		IValue[] values = new IValue[this.size + 1];
		System.arraycopy(this.values, 0, values, 0, this.size);
		values[this.size] = value;
		return new ArgumentList(values, this.size + 1);
	}

	@Override
	public IValue getFirstValue()
	{
		return this.values[0];
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
		this.values[index] = value;
	}

	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}

	@Override
	public void addValue(IValue value)
	{
		int index = this.size++;
		if (this.size > this.values.length)
		{
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}

	@Override
	public void addValue(int index, IValue value)
	{
		int i = this.size++;
		if (this.size > this.values.length)
		{
			int j = index + 1;
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			temp[index] = value;
			System.arraycopy(this.values, j, temp, j, i - j);
			this.values = temp;
		}
		else
		{
			System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
			this.values[index] = value;
		}
	}

	@Override
	public IValue getValue(int index)
	{
		if (index >= this.size)
		{
			return null;
		}
		return this.values[index];
	}

	@Override
	public IValue getValue(int index, IParameter param)
	{
		if (index >= this.size)
		{
			return null;
		}

		return this.values[index];
	}

	@Override
	public float getTypeMatch(int index, IParameter param)
	{
		if (index >= this.size)
		{
			return param.getValue() != null ? DEFAULT_MATCH : 0;
		}

		return this.values[index].getTypeMatch(param.getInternalType());
	}

	@Override
	public float getVarargsTypeMatch(int index, IParameter param)
	{
		if (index == this.size)
		{
			return VARARGS_MATCH;
		}
		if (index > this.size)
		{
			return 0;
		}

		IValue argument = this.values[index];
		IType type = param.getInternalType();
		float totalMatch = argument.getTypeMatch(type);
		if (totalMatch > 0F)
		{
			return totalMatch;
		}

		IType elementType = type.getElementType();
		for (totalMatch = 0; index < this.size; index++)
		{
			float valueMatch = this.values[index].getTypeMatch(elementType);
			if (valueMatch <= 0)
			{
				return 0F;
			}
			totalMatch += valueMatch;
		}
		return totalMatch > 0F ? totalMatch + VARARGS_MATCH : 0;
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			return;
		}

		final IType type = param.getInternalType();

		this.values[index] = TypeChecker.convertValue(this.values[index], type, typeContext, markers, context,
		                                              IArguments.argumentMarkerSupplier(param));
	}

	@Override
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			return;
		}

		final IType arrayType = param.getInternalType();

		final IValue value = this.values[index];
		if (value.isType(arrayType))
		{
			this.values[index] = TypeChecker.convertValue(value, arrayType, typeContext, markers, context,
			                                              IArguments.argumentMarkerSupplier(param));
			return;
		}

		final IType elementType = arrayType.getElementType();
		final int varargsArguments = this.size - index;
		final IValue[] values = new IValue[varargsArguments];
		final ArrayExpr arrayExpr = new ArrayExpr(values, varargsArguments);

		arrayExpr.setType(arrayType);

		for (int i = 0; i < varargsArguments; i++)
		{
			values[i] = TypeChecker.convertValue(this.values[i + index], elementType, typeContext, markers, context,
			                                     IArguments.argumentMarkerSupplier(param));
		}

		this.values[index] = arrayExpr;
		this.size = index + 1;
	}

	@Override
	public void inferType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index >= this.size)
		{
			return;
		}
		param.getInternalType().inferTypes(this.values[index].getType(), typeContext);
	}

	@Override
	public void inferVarargsType(int index, IParameter param, ITypeContext typeContext)
	{
		if (index >= this.size)
		{
			return;
		}

		IType type = this.values[index].getType();
		if (index + 1 == this.size && type.isArrayType())
		{
			param.getInternalType().inferTypes(type, typeContext);
			return;
		}

		for (int i = index + 1; i < this.size; i++)
		{
			type = Types.combine(type, this.values[i].getType());
		}

		param.getInternalType().getElementType().inferTypes(type, typeContext);
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index < this.size)
		{
			this.values[index].writeExpression(writer, param.getInternalType());
			return;
		}
		param.getValue().writeExpression(writer, param.getInternalType());
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
			this.values[i].resolveTypes(markers, context);
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
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			this.values[i].toString(prefix, buffer);
			if (i + 1 == len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}

	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			IType type = this.values[i].getType();
			if (type == null)
			{
				buffer.append("unknown");
			}
			else
			{
				type.toString("", buffer);
			}
			if (i + 1 == len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}
}
