package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IConstantValue extends IValue
{
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
}
