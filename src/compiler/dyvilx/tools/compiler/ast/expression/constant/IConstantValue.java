package dyvilx.tools.compiler.ast.expression.constant;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IConstantValue extends IValue
{
	@Override
	default boolean isResolved()
	{
		return true;
	}
	
	@Override
	default boolean isConstant()
	{
		return true;
	}

	@Override
	default boolean hasSideEffects()
	{
		return false;
	}

	@Override
	default void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	default IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	default void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	default void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	default IValue foldConstants()
	{
		return this;
	}
	
	@Override
	default IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}
	
	@Override
	default IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this;
	}
	
	@Override
	int stringSize();
	
	@Override
	boolean toStringBuilder(StringBuilder builder);
}
