package dyvilx.tools.compiler.ast.method;

import dyvil.annotation.OverloadPriority;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.type.builtin.Types;

public interface IOverloadable extends Member
{
	boolean isVariadic();

	default int getOverloadPriority()
	{
		final Annotation annotation = this.getAnnotation(Types.OVERLOADPRIORITY_CLASS);
		if (annotation == null)
		{
			return 0;
		}
		final IValue value = annotation.getArguments().getFirst();
		return value == null ? OverloadPriority.DEFAULT_PRIORITY : value.intValue();
	}
}
