package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.type.IType;

public class CombiningImplicitContext implements IImplicitContext
{
	private final IImplicitContext inner;
	private final IImplicitContext outer;

	public CombiningImplicitContext(IImplicitContext inner, IImplicitContext outer)
	{
		this.inner = inner;
		this.outer = outer;
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.inner.getImplicitMatches(list, value, targetType);

		if (!list.hasCandidate())
		{
			this.outer.getImplicitMatches(list, value, targetType);
		}
	}
}
