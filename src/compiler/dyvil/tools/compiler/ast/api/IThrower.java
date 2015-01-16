package dyvil.tools.compiler.ast.api;

import java.util.List;

public interface IThrower
{
	public void setThrows(List<IType> throwsDecls);
	
	public List<IType> getThrows();
	
	public default void addThrows(IType throwsDecl)
	{
		this.getThrows().add(throwsDecl);
	}
}
