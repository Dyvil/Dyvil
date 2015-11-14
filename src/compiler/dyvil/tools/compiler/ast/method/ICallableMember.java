package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IParameterized;

public interface ICallableMember extends IMember, IParameterized, IExceptionList, IValueConsumer
{
	public IValue getValue();
	
	@Override
	public void setValue(IValue value);
}
