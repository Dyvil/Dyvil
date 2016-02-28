package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;

public interface IMethodSignature extends ITyped, IExceptionList, ITypeParametric
{
	void addParameterType(IType type);
	
	@Override
	void setType(IType type);
}
