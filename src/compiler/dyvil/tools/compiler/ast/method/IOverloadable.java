package dyvil.tools.compiler.ast.method;

import dyvil.annotation.OverloadPriority;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.type.builtin.Types;

public interface IOverloadable extends IMember
{
	boolean isVariadic();

	default int getOverloadPriority()
	{
		final IAnnotation annotation = this.getAnnotation(Types.OVERLOADPRIORITY_CLASS);
		if (annotation == null)
		{
			return 0;
		}
		final IValue value = annotation.getArguments().getFirstValue();
		return value == null ? OverloadPriority.DEFAULT_PRIORITY : value.intValue();
	}
}
