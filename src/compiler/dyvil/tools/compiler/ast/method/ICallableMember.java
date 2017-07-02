package dyvil.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.parsing.Name;
import dyvil.source.position.SourcePosition;

public interface ICallableMember extends IClassMember, IOverloadable, IValueConsumer, ITyped, IParametric
{
	IValue getValue();

	@Override
	void setValue(IValue value);

	TypeList getExceptions();

	@Override
	default boolean isVariadic()
	{
		return this.hasModifier(Modifiers.VARARGS) || this.getParameters().isVariadic();
	}

	@Override
	default IParameter createParameter(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                  AnnotationList annotations)
	{
		return new CodeParameter(this, position, name, type, modifiers, annotations);
	}

	String getDescriptor();

	String getSignature();

	String[] getInternalExceptions();
}
