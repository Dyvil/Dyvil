package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.parameter.IParameterized;

public interface IBaseMethod extends IParameterized, IValued, IExceptionList, IAnnotationList
{
}
