package dyvil.tools.compiler.ast.method;

import java.util.List;

import dyvil.tools.compiler.ast.type.IType;

public interface IThrower
{
	public void setThrows(List<IType> throwsDecls);
	
	public List<IType> getThrows();
	
	public default void addThrows(IType throwsDecl)
	{
		this.getThrows().add(throwsDecl);
	}
}
