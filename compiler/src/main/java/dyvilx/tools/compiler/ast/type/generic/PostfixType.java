package dyvilx.tools.compiler.ast.type.generic;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.reference.ImplicitReferenceType;
import dyvilx.tools.compiler.ast.reference.ReferenceType;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class PostfixType extends NamedGenericType
{
	public PostfixType(SourcePosition position, Name name, IType lhs)
	{
		super(position, name, lhs);
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		final String unqualified = this.name.unqualified;
		final IType argument = this.arguments.get(0);
		if (unqualified.length() == 1)
		{
			switch (unqualified.charAt(0))
			{
			case '?':
				return NullableType.apply(argument).resolveType(markers, context);
			case '!':
				return new ImplicitNullableType(argument).resolveType(markers, context);
			case '*':
				return new ReferenceType(argument).resolveType(markers, context);
			case '^':
				return new ImplicitReferenceType(argument).resolveType(markers, context);
			}
		}

		return super.resolveType(markers, context);
	}

	@Override
	public String toString()
	{
		return this.arguments.get(0).toString() + this.name;
	}

	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		this.arguments.get(0).toString(indent, buffer);
		buffer.append(this.name);
	}
}
