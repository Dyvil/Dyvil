package dyvilx.tools.compiler.ast.field;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;

public interface IField extends ClassMember, IDataMember
{
	@Override
	IClass getEnclosingClass();

	@Override
	default MemberKind getKind()
	{
		return MemberKind.FIELD;
	}

	@Override
	default boolean isLocal()
	{
		return false;
	}
}
