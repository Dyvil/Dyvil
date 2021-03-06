package dyvilx.tools.compiler.ast.type.generic;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.Variance;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.WildcardType;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class PrefixType extends NamedGenericType
{
	public PrefixType(SourcePosition position, Name name)
	{
		super(position, name);
	}

	public PrefixType(SourcePosition position, Name name, IType rhs)
	{
		super(position, name, rhs);
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		final String unqualified = this.name.unqualified;
		if (unqualified.length() == 1)
		{
			final IType argument = this.arguments.get(0);
			switch (unqualified.charAt(0))
			{
			case '+':
				return new WildcardType(this.position, argument, Variance.COVARIANT).resolveType(markers, context);
			case '-':
				return new WildcardType(this.position, argument, Variance.CONTRAVARIANT).resolveType(markers, context);
			}
		}

		return super.resolveType(markers, context);
	}

	@Override
	public String toString()
	{
		return this.name + this.arguments.get(0).toString();
	}

	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		buffer.append(this.name);
		this.arguments.get(0).toString(indent, buffer);
	}
}
