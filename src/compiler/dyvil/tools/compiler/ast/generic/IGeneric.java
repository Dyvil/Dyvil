package dyvil.tools.compiler.ast.generic;

public interface IGeneric
{
	public void setGeneric();
	
	public boolean isGeneric();
	
	public int genericCount();
	
	public void setTypeVariables(ITypeVariable[] typeVars, int count);
	
	public void setTypeVariable(int index, ITypeVariable var);
	
	public void addTypeVariable(ITypeVariable var);
	
	public ITypeVariable[] getTypeVariables();
	
	public ITypeVariable getTypeVariable(int index);
}
