package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.parsing.marker.MarkerList;

public interface IConstantValue extends IValue
{
	@Override
	public default boolean isResolved()
	{
		return true;
	}
	
	@Override
	public default boolean isConstant()
	{
		return true;
	}
	
	@Override
	public default void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public default IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public default void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public default void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public default IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public default IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public default IValue toConstant(MarkerList markers)
	{
		return this;
	}
	
	@Override
	public int stringSize();
	
	@Override
	public boolean toStringBuilder(StringBuilder builder);
}
