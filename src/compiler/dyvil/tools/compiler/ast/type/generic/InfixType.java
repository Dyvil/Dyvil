package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public class InfixType extends NamedGenericType
{
	public InfixType(SourcePosition position, IType lhs, Name name, IType rhs)
	{
		super(position, name, lhs, rhs); // TODO swap lhs <-> name arguments
	}

	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		this.arguments.get(0).toString(indent, buffer);
		buffer.append(' ').append(this.name).append(' ');
		this.arguments.get(1).toString(indent, buffer);
	}

	@Override
	public String toString()
	{
		return this.arguments.get(0).toString() + ' ' + this.name + ' ' + this.arguments.get(1).toString();
	}
}
