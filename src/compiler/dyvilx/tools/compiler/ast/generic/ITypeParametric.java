package dyvilx.tools.compiler.ast.generic;

import dyvilx.tools.compiler.ast.context.IContext;

import java.util.Collections;

public interface ITypeParametric
{
	boolean isTypeParametric();

	default int typeArity()
	{
		return this.isTypeParametric() ? this.getTypeParameters().size() : 0;
	}

	TypeParameterList getTypeParameters();

	IContext getTypeParameterContext();

	static void prependTypeParameters(ITypeParametric from, ITypeParametric to)
	{
		final TypeParameterList source = from.getTypeParameters();
		final TypeParameterList dest = to.getTypeParameters();
		final int size = source.size();

		// reserve space by filling with null
		dest.addAll(0, Collections.nCopies(size, null));

		for (int i = 0; i < size; i++)
		{
			final ITypeParameter copy = source.get(i).copy();
			copy.setGeneric(to);

			// replaces the null values from the placeholder
			dest.set(i, copy);
		}
	}

	static void copyTypeParameters(ITypeParametric from, ITypeParametric to)
	{
		final TypeParameterList source = from.getTypeParameters();
		final TypeParameterList dest = to.getTypeParameters();

		dest.clear();
		dest.ensureCapacity(source.size());

		for (ITypeParameter typeParameter : source)
		{
			final ITypeParameter copy = typeParameter.copy();
			copy.setGeneric(to);
			dest.add(copy);
		}
	}
}
