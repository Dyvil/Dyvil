package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IParameterized;

public interface ICallableMember extends IMember, IParameterized, IExceptionList
{
	public IValue getValue();
	
	public void setValue(IValue value);
}
