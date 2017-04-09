package dyvil.tools.compiler.ast.generic;

public interface ITypeParametric
{
	boolean isTypeParametric();

	default int typeArity()
	{
		return this.isTypeParametric() ? this.getTypeParameters().size() : 0;
	}

	TypeParameterList getTypeParameters();
}
