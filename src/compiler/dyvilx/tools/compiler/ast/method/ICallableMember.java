package dyvilx.tools.compiler.ast.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.IParametric;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.ast.type.TypeList;

public interface ICallableMember extends ClassMember, IOverloadable, IValueConsumer, Typed, IParametric
{
	// --------------- Getters and Setters ---------------

	// - - - - - - - - Value - - - - - - - -

	IValue getValue();

	@Override
	void setValue(IValue value);

	// - - - - - - - - Exceptions - - - - - - - -

	TypeList getExceptions();

	// - - - - - - - - Parameters - - - - - - - -

	@Override
	default boolean isVariadic()
	{
		return this.hasModifier(Modifiers.ACC_VARARGS) || this.getParameters().isVariadic();
	}

	@Override
	default IParameter createParameter(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new CodeParameter(this, position, name, type, attributes);
	}

	// --------------- Compilation ---------------

	// - - - - - - - - Descriptor and Signature - - - - - - - -

	String getDescriptor();

	String getSignature();

	// - - - - - - - - Exceptions - - - - - - - -

	String[] getInternalExceptions();
}
