package dyvilx.tools.compiler.ast.statement;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IStatement extends IValue
{
	@Override
	default boolean isStatement()
	{
		return true;
	}

	@Override
	default boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	default boolean isResolved()
	{
		return true;
	}

	@Override
	default IType getType()
	{
		return Types.VOID;
	}

	@Override
	default int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return 0;
	}

	@Override
	default void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeStatement(writer);
		if (type != Types.VOID)
		{
			type.writeDefaultValue(writer);
		}
	}

	void writeStatement(MethodWriter writer) throws BytecodeException;

	static IValue checkStatement(MarkerList markers, IContext context, IValue resolvedValue, String key)
	{
		final IValue typedValue = resolvedValue.withType(Types.VOID, Types.VOID, markers, context);

		if (typedValue != null && typedValue.isUsableAsStatement())
		{
			return typedValue;
		}

		// Create an error
		final Marker marker = Markers.semantic(resolvedValue.getPosition(), key);
		marker.addInfo(Markers.getSemantic("expression.type", resolvedValue.getType()));
		markers.add(marker);

		return resolvedValue;
	}
}
