package dyvil.tools.compiler.ast.generic;

public interface ITypeParametric
{
	void setTypeParametric();
	
	boolean isTypeParametric();
	
	int typeParameterCount();
	
	void setTypeParameters(ITypeParameter[] typeParameters, int count);
	
	void setTypeParameter(int index, ITypeParameter typeParameter);
	
	void addTypeParameter(ITypeParameter typeParameter);
	
	ITypeParameter[] getTypeParameters();
	
	ITypeParameter getTypeParameter(int index);
}
