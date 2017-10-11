package dyvilx.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.IParametric;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.ITyped;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvil.lang.Name;
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
		                                  AttributeList annotations)
	{
		return new CodeParameter(this, position, name, type, modifiers, annotations);
	}

	String getDescriptor();

	String getSignature();

	String[] getInternalExceptions();
}
