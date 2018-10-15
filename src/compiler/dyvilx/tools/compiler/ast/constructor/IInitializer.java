package dyvilx.tools.compiler.ast.constructor;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;

public interface IInitializer extends ClassMember, ClassCompilable
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.INITIALIZER;
	}

	IValue getValue();

	void setValue(IValue value);
}
