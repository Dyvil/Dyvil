package dyvilx.tools.compiler.ast.context;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.type.IType;

public interface IImplicitContext
{
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);
}
