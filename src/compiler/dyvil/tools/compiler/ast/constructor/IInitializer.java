package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.backend.IClassCompilable;

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
