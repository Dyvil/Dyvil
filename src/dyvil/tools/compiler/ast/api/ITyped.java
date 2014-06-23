package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.type.Type;

public interface ITyped
{
	public void setType(Type type);
	
	public Type getType();
	
	public default boolean hasType()
	{
		return this.getType() != null;
	}
}
