package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.backend.IClassCompilable;

public interface IInitializer extends IMember, IValueConsumer, IClassCompilable
{
	IValue getValue();

	@Override
	void setValue(IValue value);
}
