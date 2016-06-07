package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.type.IType;

public interface IImplicitContext
{
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);
}
