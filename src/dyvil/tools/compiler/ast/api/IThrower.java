package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.method.ThrowsDecl;

public interface IThrower
{
	public void setThrows(List<ThrowsDecl> throwsDecls);
	
	public List<ThrowsDecl> getThrows();
	
	public default boolean addThrows(ThrowsDecl throwsDecl)
	{
		return this.getThrows().add(throwsDecl);
	}
}
