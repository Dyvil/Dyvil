package dyvilx.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.IValueList;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Arrays;
import java.util.Iterator;

public class ArgumentList implements Resolvable, IValueList
{
	// =============== Constants ===============

	public static final ArgumentList EMPTY = empty();

	private static final int DEFAULT_CAPACITY = 3;

	public static final int MISMATCH = -1;
	public static final int DEFAULT  = -2;

	// =============== Fields ===============

	protected Name @Nullable []  labels;
	protected IValue @NonNull [] values;
	protected int                size;

	// =============== Constructors ===============

	public ArgumentList()
	{
		this(DEFAULT_CAPACITY);
	}

	public ArgumentList(int capacity)
	{
		this(new IValue[capacity], 0);
	}

	public ArgumentList(IValue value)
	{
		this(new IValue[] { value }, 1);
	}

	public ArgumentList(IValue... values)
	{
		this(values, values.length);
	}

	public ArgumentList(IValue[] values, int size)
	{
		this.values = values;
		this.size = size;
	}

	public ArgumentList(Name[] labels, IValue[] values, int size)
	{
		this.labels = labels;
		this.values = values;
		this.size = size;
	}

	// =============== Static Methods ===============

	public static ArgumentList empty()
	{
		return new ArgumentList(0);
	}

	// =============== Instance Methods ===============

	// --------------- IValueList Methods ---------------

	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, 0, this.size);
	}

	// - - - - - - - - Size - - - - - - - -

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

	// - - - - - - - - Helper Methods - - - - - - - -

	protected void ensureValues(int min)
	{
		if (min >= this.values.length)
		{
			final IValue[] tempValues = new IValue[min];
			System.arraycopy(this.values, 0, tempValues, 0, this.size);
			this.values = tempValues;
		}
	}

	protected void ensureLabels()
	{
		if (this.labels == null)
		{
			this.labels = new Name[this.values.length];
			return;
		}

		this.ensureLabelsCapacity(this.values.length);
	}

	private void ensureLabelsCapacity(int min)
	{
		if (min >= this.labels.length)
		{
			final Name[] labels = new Name[min];
			System.arraycopy(this.labels, 0, labels, 0, this.size);
			this.labels = labels;
		}
	}

	private int findIndex(int index, Name label)
	{
		if (this.labels == null)
		{
			return index >= this.size ? -1 : index;
		}

		if (label != null)
		{
			// First, try to match the parameter label against the argument labels
			for (int i = 0; i < this.size; i++)
			{
				if (this.labels[i] == label)
				{
					return i;
				}
			}
		}

		// The specified label was not found, check the indices:

		if (index >= this.size)
		{
			return -1;
		}

		// Require that no argument labels exists before or at the index
		for (int i = 0; i <= index; i++)
		{
			if (this.labels[i] != null)
			{
				return -1;
			}
		}

		// No argument labels present before the requested index
		return index;
	}

	private int findNextName(int startIndex)
	{
		if (this.labels == null)
		{
			return this.size;
		}

		for (; startIndex < this.size; startIndex++)
		{
			if (this.labels[startIndex] != null)
			{
				return startIndex;
			}
		}

		return this.size;
	}

	// - - - - - - - - Get Operations - - - - - - - -

	@Override
	public IValue get(int index)
	{
		return index < this.size ? this.values[index] : null;
	}

	public IValue get(int index, Name label)
	{
		if (label == null)
		{
			return this.get(index);
		}

		final int argIndex = this.findIndex(index, label);
		if (argIndex < 0)
		{
			return null;
		}
		return this.values[argIndex];
	}

	public Name getLabel(int index)
	{
		return this.labels != null && index < this.size ? this.labels[index] : null;
	}

	// - - - - - - - - Set Operations - - - - - - - -

	@Override
	public void set(int index, IValue value)
	{
		if (index < this.size)
		{
			this.values[index] = value;
		}
	}

	public void set(int index, Name label, IValue value)
	{
		if (label == null)
		{
			this.set(index, value);
			return;
		}

		final int argIndex = this.findIndex(index, label);
		if (argIndex >= 0)
		{
			this.values[argIndex] = value;
		}
	}

	public void setLabel(int i, Name label)
	{
		if (i >= this.size)
		{
			return;
		}

		if (this.labels == null)
		{
			if (label == null)
			{
				return;
			}

			this.labels = new Name[this.values.length];
		}

		this.labels[i] = label;
	}

	// - - - - - - - - Add Operations - - - - - - - -

	@Override
	public void add(IValue value)
	{
		this.add(null, value);
	}

	@Override
	public void add(Name label, IValue value)
	{
		final int index = this.size;

		this.ensureValues(index + 1);
		this.values[index] = value;

		if (label != null || this.labels != null)
		{
			this.ensureLabels();
			this.labels[index] = label;
		}

		this.size = index + 1;
	}

	public void addAll(ArgumentList list)
	{
		this.ensureValues(this.size + list.size);
		System.arraycopy(list.values, 0, this.values, this.size, list.size);

		if (list.labels != null)
		{
			this.ensureLabelsCapacity(this.size + list.size);
			System.arraycopy(list.labels, 0, this.labels, this.size, list.size);
		}

		this.size += list.size;
	}

	// - - - - - - - - Insert Operations - - - - - - - -

	public void insert(int index, IValue value)
	{
		this.insert(index, null, value);
	}

	public void insert(int index, Name label, IValue value)
	{
		final int newSize = this.size + 1;
		if (newSize >= this.values.length)
		{
			final IValue[] values = new IValue[newSize];
			System.arraycopy(this.values, 0, values, 0, index);
			values[index] = value;
			System.arraycopy(this.values, index, values, index + 1, this.size - index);
			this.values = values;

			if (this.labels != null)
			{
				final Name[] labels = new Name[newSize];
				System.arraycopy(this.labels, 0, labels, 0, index);
				labels[index] = label;
				System.arraycopy(this.labels, index, labels, index + 1, this.size - index);
				this.labels = labels;
			}
			else if (label != null)
			{
				this.labels = new Name[newSize];
				this.labels[index] = label;
			}
		}
		else
		{
			System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
			this.values[index] = value;

			if (this.labels != null)
			{
				System.arraycopy(this.labels, index, this.labels, index + 1, this.size - index);
				this.labels[index] = label;
			}
			else if (label != null)
			{
				this.labels = new Name[newSize];
				this.labels[index] = label;
			}
		}
		this.size = newSize;
	}

	// --------------- Non-mutating Add Operations ---------------

	public ArgumentList appended(IValue value)
	{
		return this.appended(null, value);
	}

	public ArgumentList appended(Name label, IValue value)
	{
		ArgumentList copy = this.copy(this.size + 1);
		copy.add(label, value);
		return copy;
	}

	public ArgumentList concat(ArgumentList that)
	{
		final ArgumentList list = this.copy(this.size + that.size);
		list.addAll(that);
		return list;
	}

	// --------------- First and Last Getters and Setters ---------------

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

	// --------------- Parameter-based Methods ---------------

	public IValue get(IParameter parameter)
	{
		return this.get(parameter.getIndex(), parameter.getLabel());
	}

	public IValue getOrDefault(IParameter parameter)
	{
		final IValue value = this.get(parameter);
		return value != null ? value : parameter.getValue();
	}

	// --------------- Homogeneous List Methods ---------------

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

	// --------------- Phases ---------------

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
		if (this.labels == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				this.values[i].resolveTypes(markers, context);
			}

			return;
		}

		for (int i = 0; i < this.size; i++)
		{
			final Name label = this.labels[i];
			final IValue value = this.values[i];

			value.resolveTypes(markers, context);

			if (label == null)
			{
				continue;
			}

			for (int j = 0; j < i; j++)
			{
				if (this.labels[j] == label)
				{
					markers.add(Markers.semanticError(value.getPosition(), "arguments.duplicate.label", label));
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

	// --------------- Match Resolution ---------------

	public int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param,
		IImplicitContext implicitContext)
	{
		argumentIndex = this.findIndex(argumentIndex, param.getLabel());
		if (argumentIndex < 0)
		{
			return param.isVarargs() && this != EMPTY ? 0 : checkDefault(param);
		}
		if (this.getLabel(argumentIndex) == null && param.hasModifier(Modifiers.EXPLICIT))
		{
			// explicit parameters require an argument label
			return checkDefault(param);
		}

		if (!param.isVarargs())
		{
			return checkMatch(values, types, matchStartIndex + argumentIndex, this.values[argumentIndex],
			                  param.getCovariantType(), implicitContext) ? 0 : MISMATCH;
		}

		if (this == EMPTY)
		{
			return MISMATCH;
		}

		// Varargs Parameter
		final int endIndex = this.findNextName(argumentIndex + 1);
		return checkVarargsMatch(values, types, matchStartIndex, this.values, argumentIndex, endIndex, param,
		                         implicitContext);
	}

	protected static int checkDefault(IParameter param)
	{
		return param.isDefault() || param.isImplicit() ? DEFAULT : MISMATCH;
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
		if (result == IValue.MISMATCH)
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
			return checkMatch_(matchValues, matchTypes, matchIndex, argument, paramType, implicitContext) ?
				       0 :
				       MISMATCH;
		}

		if (startIndex == endIndex)
		{
			return 0;
		}

		final int count = endIndex - startIndex;
		final ArrayExpr arrayExpr = newArrayExpr(values, startIndex, count);

		if (!checkMatch_(matchValues, matchTypes, matchIndex, arrayExpr, paramType, implicitContext))
		{
			return MISMATCH;
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

	// --------------- Value and Varargs Conversion ---------------

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

	protected void resolveMissing(IParameter param, GenericData genericData, SourcePosition position,
		MarkerList markers, IContext context)
	{
		if (this == EMPTY)
		{
			// cannot infer missing arguments if the argument list is EMPTY (i.e. not denoted)

			final Marker marker = Markers.semanticError(position, "method.access.argument.empty", param.getName());
			marker.addInfo(Markers.getSemantic("method.access.argument.empty.info"));
			markers.add(marker);
			return;
		}

		if (param.isVarargs())
		{
			// varargs parameter

			final IValue value = convertValue(new ArrayExpr(position, EMPTY), param, genericData, markers, context);
			this.add(param.getLabel(), value);
			return;
		}

		if (!param.isImplicit())
		{
			// not implicit, possible default

			if (this.resolveDefault(param, context))
			{
				return;
			}

			markers.add(Markers.semanticError(position, "method.access.argument.missing", param.getName()));
			return;
		}

		// implicit parameter, possibly default

		final IType type;
		if (genericData != null)
		{
			genericData.lockAvailable();
			type = param.getCovariantType().getConcreteType(genericData);
		}
		else
		{
			type = param.getCovariantType();
		}

		final IValue implicit = context.resolveImplicit(type);
		if (implicit != null)
		{
			// make sure to resolve and type-check the implicit value
			// (implicit values should be only field accesses, but might need some capture or "this<Outer" resolution)
			final IValue value = convertValue(implicit.resolve(markers, context), param, genericData, markers, context);
			this.add(param.getLabel(), value);
			return;
		}

		// default resolution only if implicit resolution fails
		if (this.resolveDefault(param, context))
		{
			return;
		}

		markers.add(Markers.semanticError(position, "method.access.argument.implicit", param.getName(), type));
		return;
	}

	private boolean resolveDefault(IParameter param, IContext context)
	{
		if (param.isDefault())
		{
			this.add(param.getLabel(), param.getDefaultValue(context));
			return true;
		}
		return false;
	}

	public void checkValue(int index, IParameter param, GenericData genericData, SourcePosition position,
		MarkerList markers, IContext context)
	{
		index = this.findIndex(index, param.getLabel());
		if (index < 0)
		{
			this.resolveMissing(param, genericData, position, markers, context);
			return;
		}

		if (!param.isVarargs())
		{
			this.values[index] = convertValue(this.values[index], param, genericData, markers, context);
			return;
		}

		final int endIndex = this.findNextName(index + 1);
		if (!checkVarargsValue(this.values, index, endIndex, param, genericData, markers, context))
		{
			return;
		}

		final int moved = this.size - endIndex;
		if (moved > 0)
		{
			System.arraycopy(this.values, endIndex, this.values, index + 1, moved);

			if (this.labels != null)
			{
				System.arraycopy(this.labels, endIndex, this.labels, index + 1, moved);
			}
		}
		this.size = index + moved + 1;
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

	// --------------- Compilation ---------------

	public boolean hasParameterOrder()
	{
		if (this.size <= 1 || this.labels == null)
		{
			return true;
		}

		for (int i = 0; i < this.size; i++)
		{
			if (this.labels[i] != null)
			{
				return false;
			}
		}
		return true;
	}

	public final void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException
	{
		this.get(index, param.getLabel()).writeExpression(writer, param.getCovariantType());
	}

	public void writeValues(MethodWriter writer, ParameterList parameters, int startIndex) throws BytecodeException
	{
		if (this.hasParameterOrder())
		{
			for (int i = 0, count = parameters.size() - startIndex; i < count; i++)
			{
				this.writeValue(i, parameters.get(i + startIndex), writer);
			}

			return;
		}

		final int locals = writer.localCount();

		final int paramCount = parameters.size() - startIndex;

		// Step 1: Associate parameters to arguments
		final IParameter[] params = new IParameter[this.size];
		for (int i = 0; i < paramCount; i++)
		{
			final IParameter param = parameters.get(i + startIndex);
			final int argIndex = this.findIndex(i, param.getLabel());
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
				this.values[i].writeExpression(writer, param.getCovariantType());
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
				final IType parameterType = parameter.getCovariantType();
				final IValue value = this.values[i];

				final int localIndex = value.writeStore(writer, parameterType);
				targets[parameter.getIndex() - startIndex] = localIndex;
			}

			// Step 4: Load the local variables in order
			for (int i = 0; i < paramCount; i++)
			{
				final IParameter parameter = parameters.get(i + startIndex);
				final int target = targets[parameter.getIndex() - startIndex];

				switch (target)
				{
				case -1:
				case -2:
					// Value for parameter was already written in Step 2
					continue;
				default:
					// Value for parameter exists -> load the variable
					writer.visitVarInsn(parameter.getCovariantType().getLoadOpcode(), target);
				}
			}
		}

		writer.resetLocals(locals);
	}

	// --------------- Formatting ---------------

	@Override
	public final String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.toString(indent, buffer, '(', ')');
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer, char open, char close)
	{
		Formatting.appendSeparator(buffer, "parameters.open_paren", open);

		if (this.size > 0)
		{
			this.appendValue(indent, buffer, 0);
			for (int i = 1; i < this.size; i++)
			{
				Formatting.appendSeparator(buffer, "parameters.separator", ',');
				this.appendValue(indent, buffer, i);
			}
		}

		Formatting.appendClose(buffer, "parameters.close_paren", close);
	}

	public void appendValue(@NonNull String indent, @NonNull StringBuilder buffer, int index)
	{
		final Name label = this.getLabel(index);
		if (label != null)
		{
			buffer.append(label);
			Formatting.appendSeparator(buffer, "parameters.name_value_separator", ':');
		}

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
		final Name label = this.getLabel(index);
		if (label != null)
		{
			buffer.append(label).append(": ");
		}

		this.values[index].getType().toString("", buffer);
	}

	// --------------- Copying ---------------

	public ArgumentList copy()
	{
		return this.copy(this.size);
	}

	public ArgumentList copy(int capacity)
	{
		return new ArgumentList(this.labels == null ? null : Arrays.copyOf(this.labels, capacity),
		                        Arrays.copyOf(this.values, capacity), this.size);
	}
}
