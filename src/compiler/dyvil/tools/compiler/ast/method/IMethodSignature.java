package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.generic.ITypeParameterized;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;

public interface IMethodSignature extends ITyped, IExceptionList, ITypeParameterized
{
	void addParameterType(IType type);
	
	@Override
	void setType(IType type);
}
