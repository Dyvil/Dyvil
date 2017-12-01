package dyvilx.tools.compiler.ast.generic;

import dyvilx.tools.compiler.ast.context.IContext;

public interface ITypeParametric
{
	boolean isTypeParametric();

	default int typeArity()
	{
		return this.isTypeParametric() ? this.getTypeParameters().size() : 0;
	}

	TypeParameterList getTypeParameters();

	IContext getTypeParameterContext();
}
