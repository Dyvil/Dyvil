package dyvil.tools.compiler.ast.parameter;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Arrays;
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
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param, IImplicitContext implicitContext)
	{
		if (argumentIndex > this.size)
		{
			return -1;
		}
		if (argumentIndex == this.size)
		{
			return param.isVarargs() ? 0 : -1;
		}

		if (param.isVarargs())
		{
			return checkVarargsMatch(values, types, matchStartIndex, this.values, argumentIndex, this.size, param,
			                         implicitContext);
		}
		return checkMatch(values, types, matchStartIndex + argumentIndex, this.values[argumentIndex],
		                  param.getInternalType(), implicitContext) ? 0 : -1;
	}

	protected static boolean checkMatch(int[] matchValues, IType[] matchTypes, int matchIndex, IValue argument, IType type, IImplicitContext implicitContext)
	{
		return !argument.checkVarargs(false) && checkMatch_(matchValues, matchTypes, matchIndex, argument, type,
		                                                    implicitContext);
	}

	private static boolean checkMatch_(int[] matchValues, IType[] matchTypes, int matchIndex, IValue argument, IType type, IImplicitContext implicitContext)
	{
		final int result = TypeChecker.getTypeMatch(argument, type, implicitContext);
		if (result == 0)
		{
			return false;
		}

		matchValues[matchIndex] = result;
		matchTypes[matchIndex] = type;
		return true;
	}

	protected static int checkVarargsMatch(int[] matchValues, IType[] matchTypes, int matchStartIndex, //
		                                      IValue[] values, int startIndex, int endIndex, //
		                                      IParameter param, IImplicitContext implicitContext)
	{
		final IValue argument = values[startIndex];
		final IType arrayType = param.getInternalType();
		if (argument.checkVarargs(false))
		{
			return checkMatch_(matchValues, matchTypes, matchStartIndex, argument, arrayType, implicitContext) ? 0 : -1;
		}

		if (startIndex == endIndex)
		{
			return 0;
		}

		final int count = endIndex - startIndex;
		final IType elementType = arrayType.getElementType();
		for (; startIndex < endIndex; startIndex++)
		{
			if (!checkMatch(matchValues, matchTypes, matchStartIndex + startIndex, values[startIndex], elementType,
			                implicitContext))
			{
				return -1;
			}
		}
		return count;
	}

	@Override
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			return;
		}

		if (param.isVarargs())
		{
			if (checkVarargsValue(this.values, index, this.size, param, typeContext, markers, context))
			{
				this.size = index + 1;
			}
			return;
		}

		final IType type = param.getInternalType();

		this.values[index] = TypeChecker.convertValue(this.values[index], type, typeContext, markers, context,
		                                              IArguments.argumentMarkerSupplier(param));
	}

	protected static boolean checkVarargsValue(IValue[] values, int startIndex, int endIndex, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType arrayType = param.getInternalType();

		final IValue value = values[startIndex];
		if (value.checkVarargs(true))
		{
			values[startIndex] = TypeChecker.convertValue(value, arrayType, typeContext, markers, context,
			                                              IArguments.argumentMarkerSupplier(param));
			return false;
		}

		final IType elementType = arrayType.getElementType();
		final int varargsArguments = endIndex - startIndex;
		final IValue[] arrayValues = new IValue[varargsArguments];
		final ArrayExpr arrayExpr = new ArrayExpr(arrayValues, varargsArguments);

		arrayExpr.setType(arrayType);

		for (int i = 0; i < varargsArguments; i++)
		{
			arrayValues[i] = TypeChecker
				                 .convertValue(values[i + startIndex], elementType, typeContext, markers, context,
				                               IArguments.argumentMarkerSupplier(param));
		}

		values[startIndex] = arrayExpr;
		return true;
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		if (index < this.size)
		{
			this.values[index].writeExpression(writer, param.getInternalType());
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
	public IArguments copy()
	{
		return new ArgumentList(Arrays.copyOf(this.values, this.size), this.size);
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
