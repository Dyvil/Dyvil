package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.type.IType;

public interface IImplicitContext
{
	void getImplicitMatches(MethodMatchList list, IValue value, IType targetType);
}
