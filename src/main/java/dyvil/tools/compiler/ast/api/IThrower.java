package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.type.Type;

public interface IThrower
{
	public void setThrows(List<Type> throwsDecls);
	
	public List<Type> getThrows();
	
	public default void addThrows(Type throwsDecl)
	{
		this.getThrows().add(throwsDecl);
	}
}
