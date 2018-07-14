package dyvilx.tools.compiler.ast.constructor;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.header.ClassCompilable;

public interface IInitializer extends ClassMember, IValueConsumer, ClassCompilable
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.INITIALIZER;
	}

	IValue getValue();

	@Override
	void setValue(IValue value);
}
