package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IParameterized;

public interface ICallableMember extends IMember, IParameterized, IValued, IExceptionList
{
}
