package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;

public interface IField extends IClassMember, IDataMember
{
	@Override
	IClass getEnclosingClass();

	@Override
	default MemberKind getKind()
	{
		return MemberKind.FIELD;
	}

	@Override
	default boolean isField()
	{
		return true;
	}

	@Override
	default boolean isVariable()
	{
		return false;
	}
}
