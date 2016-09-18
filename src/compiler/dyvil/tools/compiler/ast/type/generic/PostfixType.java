package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class PostfixType extends NamedGenericType
{
	public PostfixType(ICodePosition position, Name name, IType lhs)
	{
		super(position, name, new IType[] { lhs }, 1);
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
