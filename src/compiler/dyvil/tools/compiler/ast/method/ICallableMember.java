package dyvil.tools.compiler.ast.method;

import dyvil.annotation.OverloadPriority;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;

public interface ICallableMember extends IMember, IValueConsumer, ITyped, IParametric, IExceptionList
{
	IValue getValue();

	@Override
	void setValue(IValue value);

	@Override
	default boolean isVariadic()
	{
		return this.hasModifier(Modifiers.VARARGS) || this.getParameterList().isVariadic();
	}

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
