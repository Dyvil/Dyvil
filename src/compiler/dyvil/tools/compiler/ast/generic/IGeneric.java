package dyvil.tools.compiler.ast.generic;

import java.util.List;

public interface IGeneric
{
	public void setGeneric();
	
	public boolean isGeneric();
	
	public void setTypeVariables(List<ITypeVariable> list);
	
	public List<ITypeVariable> getTypeVariables();
	
	public void addTypeVariable(ITypeVariable var);
}
