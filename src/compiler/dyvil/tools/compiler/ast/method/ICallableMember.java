package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IParametric;

public interface ICallableMember extends IMember, IParametric, IExceptionList, IValueConsumer
{
	IValue getValue();
	
	@Override
	void setValue(IValue value);
}
