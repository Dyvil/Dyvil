package dyvil.tools.compiler.ast.generic;

public interface IGeneric
{
	void setGeneric();
	
	boolean isGeneric();
	
	int genericCount();
	
	void setTypeVariables(ITypeVariable[] typeVars, int count);
	
	void setTypeVariable(int index, ITypeVariable var);
	
	void addTypeVariable(ITypeVariable var);
	
	ITypeVariable[] getTypeVariables();
	
	ITypeVariable getTypeVariable(int index);
}
