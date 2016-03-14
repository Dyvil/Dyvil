package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;

public interface ICallableSignature extends ITyped, IParametric, IExceptionList
{
	void addParameterType(IType type);
}
