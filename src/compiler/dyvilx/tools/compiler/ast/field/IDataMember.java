package dyvilx.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IDataMember extends IMember, IAccessible, IValueConsumer
{
	IValue getValue();

	@Override
	void setValue(IValue value);

	default IProperty getProperty()
	{
		return null;
	}

	default IProperty createProperty()
	{
		return null;
	}

	default void setProperty(IProperty property)
	{
	}

	IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context);

	default IValue checkAssign(MarkerList markers, IContext context, SourcePosition position, IValue receiver, IValue newValue)
	{
		if (this.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semanticError(position, this.getKind().getName() + ".assign.final", this.getName()));
		}

		final IType type = this.getType();
		final ITypeContext typeContext = receiver == null ? ITypeContext.NULL : receiver.getType();

		final TypeChecker.MarkerSupplier markerSupplier = (errorPosition, expected, actual) -> {
			final String kindName = this.getKind().getName();
			final Marker marker = Markers.semanticError(errorPosition, kindName + ".assign.type", this.getName());
			marker.addInfo(Markers.getSemantic(kindName + ".type", expected));
			marker.addInfo(Markers.getSemantic("value.type", actual));
			return marker;
		};

		return TypeChecker.convertValue(newValue, type, typeContext, markers, context, markerSupplier);
	}

	default IClass getEnclosingClass()
	{
		return null;
	}

	boolean isLocal();

	default boolean hasConstantValue()
	{
		return false;
	}

	// Compilation

	@Override
	default void writeGet(MethodWriter writer) throws BytecodeException
	{
		this.writeGet(writer, null, 0);
	}

	void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException;

	default void writeGet_Unwrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{

	}

	default boolean writeSet_PreValue(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		return false;
	}

	default void writeSet_Wrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
	}

	void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException;

	default void writeReceiver(MethodWriter writer, IValue receiver)
	{
		if (receiver != null)
		{
			final IType receiverType = this.getEnclosingClass().getReceiverType();
			if (this.hasModifier(Modifiers.STATIC))
			{
				receiver.writeNullCheckedExpression(writer, receiverType);
			}
			else
			{
				receiver.writeExpression(writer, receiverType);
			}
		}
	}

	default void writeGet(MethodWriter writer, IValue receiver, int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, receiver);

		this.writeGet_Get(writer, lineNumber);
		this.writeGet_Unwrap(writer, lineNumber);
	}

	default void writeSet(MethodWriter writer, IValue receiver, IValue value, int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, receiver);

		this.writeSet_PreValue(writer, lineNumber);
		value.writeExpression(writer, this.getType());

		this.writeSet_Wrap(writer, lineNumber);
		this.writeSet_Set(writer, lineNumber);
	}

	default String getDescriptor()
	{
		return this.getType().getExtendedName();
	}

	default String getSignature()
	{
		return this.getType().getSignature();
	}

	default IDataMember capture(IContext context)
	{
		return this;
	}

	static void toString(String prefix, StringBuilder buffer, IMember field, String key)
	{
		final IType type = field.getType();

		appendKeyword(buffer, field);
		buffer.append(' ').append(field.getName());

		if (type != null && type != Types.UNKNOWN)
		{
			Formatting.appendSeparator(buffer, key, ':');
			type.toString(prefix, buffer);
		}
	}

	static void appendKeyword(StringBuilder buffer, IMember member)
	{
		if (member.hasModifier(Modifiers.ENUM_CONST))
		{
			buffer.append("enum const");
			return;
		}
		if (member.hasModifier(Modifiers.FINAL) && (member.getKind() != MemberKind.PROPERTY))
		{
			buffer.append("let");
			return;
		}

		buffer.append("var");
	}
}
