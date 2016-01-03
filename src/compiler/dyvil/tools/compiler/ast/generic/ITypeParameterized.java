package dyvil.tools.compiler.ast.generic;

public interface ITypeParameterized
{
	void setTypeParameterized();
	
	boolean isTypeParameterized();
	
	int typeParameterCount();
	
	void setTypeParameters(ITypeParameter[] typeVars, int count);
	
	void setTypeParameter(int index, ITypeParameter var);
	
	void addTypeParameter(ITypeParameter var);
	
	ITypeParameter[] getTypeParameters();
	
	ITypeParameter getTypeParameter(int index);
}
