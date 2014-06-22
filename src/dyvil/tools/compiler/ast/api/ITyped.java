package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.type.Type;

public interface ITyped
{
	public void setType(Type type);
	
	public Type getType();
}
