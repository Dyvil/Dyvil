package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class InfixType extends NamedGenericType
{
	public InfixType(ICodePosition position, IType lhs, Name name, IType rhs)
	{
		super(position, name, new IType[] { lhs, rhs }, 2);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.typeArguments[0].toString(prefix, buffer);
		buffer.append(' ').append(this.name).append(' ');
		this.typeArguments[1].toString(prefix, buffer);
	}

	@Override
	public String toString()
	{
		return this.typeArguments[0].toString() + ' ' + this.name + ' ' + this.typeArguments[1].toString();
	}
}
