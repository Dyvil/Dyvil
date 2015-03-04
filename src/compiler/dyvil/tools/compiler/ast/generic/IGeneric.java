package dyvil.tools.compiler.ast.generic;


public interface IGeneric
{
	public void setGeneric();
	
	public boolean isGeneric();
	
	public int genericCount();
	
	public void setTypeVariable(int index, ITypeVariable var);
	
	public void addTypeVariable(ITypeVariable var);
	
	public ITypeVariable getTypeVariable(int index);
}
