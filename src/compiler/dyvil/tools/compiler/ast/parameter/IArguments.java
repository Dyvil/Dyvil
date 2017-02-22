package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IArguments extends IASTNode, IResolvable, Iterable<IValue>
{
	static IValue convertValue(IValue value, IParameter parameter, GenericData genericData, MarkerList markers,
		                          IContext context)
	{
		if (genericData != null && value.isPolyExpression())
		{
			// Lock available type arguments before type-checking a poly-expression
			genericData.lockAvailable();
		}

		final IType type = parameter.getInternalType();
		final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("method.access.argument_type",
		                                                                             parameter.getName());
		return TypeChecker.convertValue(value, type, genericData, markers, context, markerSupplier);
	}

	@Override
	default ICodePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(ICodePosition position)
	{
	}

	int size();

	boolean isEmpty();

	// 'Variations'

	IArguments withLastValue(IValue value);

	default IArguments withLastValue(Name name, IValue value)
	{
		return this.withLastValue(value);
	}

	// First Values

	IValue getFirstValue();

	void setFirstValue(IValue value);

	// Last Values

	IValue getLastValue();

	void setLastValue(IValue value);

	// Used by Methods

	void setValue(int index, IParameter param, IValue value);

	IValue getValue(int index, IParameter param);

	int checkMatch(int[] values, IType[] types, int matchStartIndex, int argumentIndex, IParameter param,
		              IImplicitContext implicitContext);

	void checkValue(int index, IParameter param, GenericData genericData, MarkerList markers, IContext context);

	/**
	 * Returns {@code true} if the arguments of this list are in the order of the parameters. {@link NamedArgumentList}s
	 * can have arbitrary order, so they must return {@code false}.
	 *
	 * @return true if the arguments of this list are in the order of the parameters.
	 */
	default boolean hasParameterOrder()
	{
		return true;
	}

	default void writeValues(MethodWriter writer, IParameterList parameters, int startIndex) throws BytecodeException
	{
		for (int i = 0, count = parameters.size() - startIndex; i < count; i++)
		{
			this.writeValue(i, parameters.get(i + startIndex), writer);
		}
	}

	void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException;

	// Phase Methods

	boolean isResolved();

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	void check(MarkerList markers, IContext context);

	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	IArguments copy();

	@Override
	void toString(String prefix, StringBuilder buffer);

	default String typesToString()
	{
		final StringBuilder builder = new StringBuilder();
		this.typesToString(builder);
		return builder.toString();
	}

	void typesToString(StringBuilder buffer);
}
