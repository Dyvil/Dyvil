package dyvil.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Arrays;
import java.util.Iterator;

public class ArgumentList implements IArguments, IValueList
{
	public static final ArgumentList EMPTY = empty();

	protected IValue[] values;
	protected int      size;

	public ArgumentList()
	{
		this.values = new IValue[3];
	}

	public ArgumentList(IValue value)
	{
		this.values = new IValue[] { value };
		this.size = 1;
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

	public static ArgumentList empty()
	{
		return new ArgumentList(new IValue[0], 0);
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
		return this.size <= 0 ? null : this.values[0];
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
	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param,
		                     IImplicitContext implicitContext)
	{
		if (argumentIndex >= this.size)
		{
			return param.isVarargs() && this != EMPTY ? 0 : -1;
		}
		if (param.hasModifier(Modifiers.EXPLICIT))
		{
			return -1;
		}

		if (!param.isVarargs())
		{
			return checkMatch(values, types, matchStartIndex + argumentIndex, this.values[argumentIndex],
			                  param.getCovariantType(), implicitContext) ? 0 : -1;
		}

		if (this == EMPTY)
		{
			return -1;
		}

		return checkVarargsMatch(values, types, matchStartIndex, this.values, argumentIndex, this.size, param,
		                         implicitContext);
	}

	protected static boolean checkMatch(int[] matchValues, IType[] matchTypes, int matchIndex, IValue argument,
		                                   IType type, IImplicitContext implicitContext)
	{
		return !argument.checkVarargs(false) && checkMatch_(matchValues, matchTypes, matchIndex, argument, type,
		                                                    implicitContext);
	}

	private static boolean checkMatch_(int[] matchValues, IType[] matchTypes, int matchIndex, IValue argument,
		                                  IType type, IImplicitContext implicitContext)
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
		final IType paramType = param.getCovariantType();
		final int matchIndex = matchStartIndex + startIndex;
		if (argument.checkVarargs(false))
		{
			return checkMatch_(matchValues, matchTypes, matchIndex, argument, paramType, implicitContext) ? 0 : -1;
		}

		if (startIndex == endIndex)
		{
			return 0;
		}

		final int count = endIndex - startIndex;
		final ArrayExpr arrayExpr = newArrayExpr(values, startIndex, count);

		if (!checkMatch_(matchValues, matchTypes, matchIndex, arrayExpr, paramType, implicitContext))
		{
			return -1;
		}

		// We fill the remaining entries that are reserved for the (now wrapped) varargs values with the match value
		// of the array expression and the element type
		final int value = matchValues[matchIndex];
		final IType type = arrayExpr.getElementType();
		for (int i = 0; i < count; i++)
		{
			matchValues[matchIndex + i] = value;
			matchTypes[matchIndex + i] = type;
		}
		return count;
	}

	private static ArrayExpr newArrayExpr(IValue[] values, int startIndex, int count)
	{
		final IValue[] arrayValues = new IValue[count];
		System.arraycopy(values, startIndex, arrayValues, 0, count);
		return new ArrayExpr(arrayValues, count);
	}

	@Override
	public void checkValue(int index, IParameter param, GenericData genericData, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			if (param.isVarargs())
			{
				final ArrayExpr arrayExpr = new ArrayExpr(new IValue[0], 0);
				final IValue converted = IArguments.convertValue(arrayExpr, param, genericData, markers, context);
				this.addValue(converted);
			}
			return;
		}

		if (!param.isVarargs())
		{
			this.values[index] = IArguments.convertValue(this.values[index], param, genericData, markers, context);
			return;
		}

		if (!checkVarargsValue(this.values, index, this.size, param, genericData, markers, context))
		{
			return;
		}

		for (int i = index + 1; i < this.size; i++)
		{
			this.values[i] = null;
		}
		this.size = index + 1;
	}

	protected static boolean checkVarargsValue(IValue[] values, int startIndex, int endIndex, IParameter param,
		                                          GenericData genericData, MarkerList markers, IContext context)
	{
		final IValue value = values[startIndex];
		if (value.checkVarargs(true))
		{
			values[startIndex] = IArguments.convertValue(value, param, genericData, markers, context);
			return false;
		}

		final int count = endIndex - startIndex;
		final ArrayExpr arrayExpr = newArrayExpr(values, startIndex, count);
		final IValue converted = IArguments.convertValue(arrayExpr, param, genericData, markers, context);

		values[startIndex] = converted;
		return true;
	}

	@Override
	public void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		this.values[index].writeExpression(writer, param.getCovariantType());
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
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		if (this.size > 0)
		{
			this.appendValue(prefix, buffer, 0);
			for (int i = 1; i < this.size; i++)
			{
				Formatting.appendSeparator(buffer, "parameters.separator", ',');
				this.appendValue(prefix, buffer, i);
			}
		}

		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	protected void appendValue(@NonNull String indent, @NonNull StringBuilder buffer, int index)
	{
		this.values[index].toString(indent, buffer);
	}

	@Override
	public void typesToString(StringBuilder buffer)
	{
		buffer.append('(');
		if (this.size > 0)
		{
			this.appendType(buffer, 0);
			for (int i = 1; i < this.size; i++)
			{
				buffer.append(", ");
				this.appendType(buffer, i);
			}
		}
		buffer.append(')');
	}

	protected void appendType(@NonNull StringBuilder buffer, int index)
	{
		this.values[index].getType().toString("", buffer);
	}
}
