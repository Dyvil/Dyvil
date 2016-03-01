package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;

public interface ICallableMember extends IMember, ICallableSignature, IValueConsumer
{
	IValue getValue();
	
	@Override
	void setValue(IValue value);
}
