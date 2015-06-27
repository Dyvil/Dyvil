package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.member.IClassMember;

public interface IField extends IClassMember, IDataMember
{
	@Override
	public default boolean isField()
	{
		return true;
	}
	
	@Override
	public default boolean isVariable()
	{
		return false;
	}
}
