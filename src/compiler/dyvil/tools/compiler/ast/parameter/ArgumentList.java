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
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Arrays;
import java.util.Iterator;

public class ArgumentList implements IResolvable, IValueList
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

	// List Methods

	public static ArgumentList empty()
	{
		return new ArgumentList(new IValue[0], 0);
	}

	public IValue[] getArray()
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
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	public ArgumentList appended(IValue value)
	{
		IValue[] values = new IValue[this.size + 1];
		System.arraycopy(this.values, 0, values, 0, this.size);
		values[this.size] = value;
		return new ArgumentList(values, this.size + 1);
	}

	public ArgumentList appended(Name name, IValue value)
	{
		return this.appended(value);
	}

	public ArgumentList concat(ArgumentList that)
	{
		final IValue[] values = new IValue[this.size + that.size];
		System.arraycopy(this.values, 0, values, 0, this.size);
		System.arraycopy(that.values, 0, values, this.size, that.size);

		if (that instanceof NamedArgumentList)
		{
			final Name[] keys = new Name[this.size + that.size];
			System.arraycopy(((NamedArgumentList) that).keys, 0, keys, this.size, 0);
			return new NamedArgumentList(keys, values, this.size + that.size);
		}
		return new ArgumentList(values);
	}

	public IValue getFirst()
	{
		return this.size <= 0 ? null : this.values[0];
	}

	public void setFirst(IValue value)
	{
		this.values[0] = value;
	}

	public IValue getLast()
	{
		return this.values[this.size - 1];
	}

	public void setLast(IValue value)
	{
		this.values[this.size - 1] = value;
	}

	public void set(int index, Name key, IValue value)
	{
		this.values[index] = value;
	}

	@Override
	public void set(int index, IValue value)
	{
		this.values[index] = value;
	}

	@Override
	public void add(IValue value)
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

	public void insert(int index, IValue value)
	{
		this.insert(index, null, value);
	}

	public void insert(int index, Name key, IValue value)
	{
		final int newSize = this.size + 1;
		if (newSize >= this.values.length)
		{
			final IValue[] temp = new IValue[newSize];
			System.arraycopy(this.values, 0, temp, 0, index);
			temp[index] = value;
			System.arraycopy(this.values, index, temp, index + 1, this.size - index);
			this.values = temp;
		}
		else
		{
			System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
		}
		this.size = newSize;
	}

	@Override
	public IValue get(int index)
	{
		if (index >= this.size)
		{
			return null;
		}
		return this.values[index];
	}

	public IValue get(IParameter parameter)
	{
		return this.get(parameter.getIndex(), parameter.getLabel());
	}

	public IValue get(int index, Name key)
	{
		return this.get(index);
	}

	// Utilities for Homogeneous Lists

	public IType getCommonType()
	{
		if (this.size == 0)
		{
			return Types.ANY;
		}

		IType type = this.values[0].getType();
		for (int i = 1; i < this.size; i++)
		{
			final IType valueType = this.values[i].getType();
			type = Types.combine(type, valueType);
		}

		return type;
	}

	public boolean isType(IType type)
	{
		if (this.size == 0)
		{
			return true;
		}

		for (int i = 0; i < this.size; i++)
		{
			if (!this.values[i].isType(type))
			{
				return false;
			}
		}

		return true;
	}

	// Resolution

	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.size == 0)
		{
			return IValue.EXACT_MATCH;
		}

		int min = Integer.MAX_VALUE;
		for (int i = 0; i < this.size; i++)
		{
			final int match = TypeChecker.getTypeMatch(this.values[i], type, implicitContext);
			if (match == IValue.MISMATCH)
			{
				return IValue.MISMATCH;
			}
			if (match < min)
			{
				min = match;
			}
		}

		return min;
	}

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

	static IValue convertValue(IValue value, IParameter parameter, GenericData genericData, MarkerList markers,
		                          IContext context)
	{
		if (genericData != null && value.isPolyExpression())
		{
			// Lock available type arguments before type-checking a poly-expression
			genericData.lockAvailable();
		}

		final IType type = parameter.getCovariantType();
		final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("method.access.argument_type",
		                                                                             parameter.getName());
		return TypeChecker.convertValue(value, type, genericData, markers, context, markerSupplier);
	}

	private static ArrayExpr newArrayExpr(IValue[] values, int startIndex, int count)
	{
		final IValue[] arrayValues = new IValue[count];
		System.arraycopy(values, startIndex, arrayValues, 0, count);
		return new ArrayExpr(new ArgumentList(arrayValues, count));
	}

	public void checkValue(int index, IParameter param, GenericData genericData, MarkerList markers, IContext context)
	{
		if (index >= this.size)
		{
			if (param.isVarargs())
			{
				final ArrayExpr arrayExpr = new ArrayExpr(EMPTY);
				final IValue converted = convertValue(arrayExpr, param, genericData, markers, context);
				this.add(converted);
			}
			return;
		}

		if (!param.isVarargs())
		{
			this.values[index] = convertValue(this.values[index], param, genericData, markers, context);
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
			values[startIndex] = convertValue(value, param, genericData, markers, context);
			return false;
		}

		final int count = endIndex - startIndex;
		final ArrayExpr arrayExpr = newArrayExpr(values, startIndex, count);
		final IValue converted = convertValue(arrayExpr, param, genericData, markers, context);

		values[startIndex] = converted;
		return true;
	}

	// Compilation

	public boolean hasParameterOrder()
	{
		return true;
	}

	public final void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		final IValue value = this.get(index, param.getLabel());
		if (value == null)
		{
			param.writeGetDefaultValue(writer);
			return;
		}
		value.writeExpression(writer, param.getCovariantType());
	}

	public void writeValues(MethodWriter writer, ParameterList parameters, int startIndex) throws BytecodeException
	{
		for (int i = 0, count = parameters.size() - startIndex; i < count; i++)
		{
			this.writeValue(i, parameters.get(i + startIndex), writer);
		}
	}

	// Phases

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

	// String Conversion

	@Override
	public final String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		if (this.size > 0)
		{
			this.appendValue(indent, buffer, 0);
			for (int i = 1; i < this.size; i++)
			{
				Formatting.appendSeparator(buffer, "parameters.separator", ',');
				this.appendValue(indent, buffer, i);
			}
		}

		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
	}

	protected void appendValue(@NonNull String indent, @NonNull StringBuilder buffer, int index)
	{
		this.values[index].toString(indent, buffer);
	}

	public final String typesToString()
	{
		final StringBuilder builder = new StringBuilder();
		this.typesToString(builder);
		return builder.toString();
	}

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

	// Copying

	public ArgumentList copy()
	{
		return new ArgumentList(Arrays.copyOf(this.values, this.size), this.size);
	}

	public NamedArgumentList toNamed()
	{
		return new NamedArgumentList(new Name[this.values.length], this.values, this.size);
	}
}
