package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.reference.ImplicitReferenceType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvil.tools.compiler.ast.type.compound.NullableType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class PostfixType extends NamedGenericType
{
	public PostfixType(ICodePosition position, Name name, IType lhs)
	{
		super(position, name, new IType[] { lhs }, 1);
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		final String unqualified = this.name.unqualified;
		if (unqualified.length() == 1)
		{
			switch (unqualified.charAt(0))
			{
			case '?':
				return new NullableType(this.typeArguments[0]).resolveType(markers, context);
			case '!':
				return new ImplicitNullableType(this.typeArguments[0]).resolveType(markers, context);
			case '*':
				return new ReferenceType(this.typeArguments[0]).resolveType(markers, context);
			case '^':
				return new ImplicitReferenceType(this.typeArguments[0]).resolveType(markers, context);
			}
		}

		return super.resolveType(markers, context);
	}

	@Override
	public String toString()
	{
		return this.typeArguments[0].toString() + this.name;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.typeArguments[0].toString(prefix, buffer);
		buffer.append(this.name);
	}
}
