package dyvilx.tools.compiler.ast.constructor;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.header.IClassCompilable;

public interface IInitializer extends IClassMember, IValueConsumer, IClassCompilable
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
