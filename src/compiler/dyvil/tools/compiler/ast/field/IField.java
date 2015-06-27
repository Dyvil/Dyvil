package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IClassMember;

public interface IField extends IClassMember, IDataMember, IClassCompilable
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
