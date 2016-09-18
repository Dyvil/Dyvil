package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
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
