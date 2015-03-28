package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.value.IValued;

public interface IBaseMethod extends IParameterized, IValued, IExceptionList, IAnnotationList
{
}
