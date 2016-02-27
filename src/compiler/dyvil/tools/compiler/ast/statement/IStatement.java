package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public interface IStatement extends IValue
{
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
	default IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	default boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	default float getTypeMatch(IType type)
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

		if (typedValue == null || !typedValue.isUsableAsStatement())
		{
			final Marker marker = Markers.semantic(resolvedValue.getPosition(), key);
			marker.addInfo(Markers.getSemantic("return.type", resolvedValue.getType()));
			markers.add(marker);
			return resolvedValue;
		}

		return typedValue;
	}

	static IValue checkCondition(MarkerList markers, IContext context, IValue resolvedValue, String key)
	{
		final IValue typedValue = resolvedValue.withType(Types.BOOLEAN, Types.BOOLEAN, markers, context);

		if (typedValue == null)
		{
			final Marker marker = Markers.semantic(resolvedValue.getPosition(), key);
			marker.addInfo(Markers.getSemantic("value.type", resolvedValue.getType()));
			markers.add(marker);
			return resolvedValue;
		}

		return typedValue;
	}
}
