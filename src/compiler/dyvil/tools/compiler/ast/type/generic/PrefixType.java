package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.WildcardType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class PrefixType extends NamedGenericType
{
	public PrefixType(ICodePosition position, Name name)
	{
		super(position, name);
	}

	public PrefixType(ICodePosition position, Name name, IType rhs)
	{
		super(position, name, new IType[] { rhs }, 1);
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		final String unqualified = this.name.unqualified;
		if (unqualified.length() == 1)
		{
			switch (unqualified.charAt(0))
			{
			case '+':
				return new WildcardType(this.position, this.typeArguments[0], Variance.COVARIANT)
					       .resolveType(markers, context);
			case '-':
				return new WildcardType(this.position, this.typeArguments[0], Variance.CONTRAVARIANT)
					       .resolveType(markers, context);
			}
		}

		return super.resolveType(markers, context);
	}

	@Override
	public String toString()
	{
		return this.name + this.typeArguments[0].toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		this.typeArguments[0].toString(prefix, buffer);
	}
}
