package dyvil.tools.compiler.ast.method;

import dyvil.annotation.OverloadPriority;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

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

	@Override
	default IParameter createParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                                  AnnotationList annotations)
	{
		return new CodeParameter(this, position, name, type, modifiers, annotations);
	}
}
