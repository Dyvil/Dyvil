package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IDataMember extends IMember, IAccessible, IValueConsumer
{
	IValue getValue();
	
	@Override
	void setValue(IValue value);
	
	IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context);
	
	IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue);
	
	default boolean isEnumConstant()
	{
		return this.hasModifier(Modifiers.ENUM);
	}
	
	default IClass getTheClass()
	{
		return null;
	}
	
	boolean isField();
	
	boolean isVariable();
	
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
	
	default void writeGet(MethodWriter writer, IValue receiver, int lineNumber) throws BytecodeException
	{
		if (receiver != null)
		{
			receiver.writeExpression(writer, this.getTheClass().getType());
		}

		this.writeGet_Get(writer, lineNumber);
		this.writeGet_Unwrap(writer, lineNumber);
	}
	
	default void writeSet(MethodWriter writer, IValue receiver, IValue value, int lineNumber) throws BytecodeException
	{
		if (receiver != null)
		{
			receiver.writeExpression(writer, this.getTheClass().getType());
		}

		this.writeSet_PreValue(writer, lineNumber);
		value.writeExpression(writer, this.getType());

		this.writeSet_Wrap(writer, lineNumber);
		this.writeSet_Set(writer, lineNumber);
	}
	
	String getDescription();
	
	String getSignature();
	
	default IDataMember capture(IContext context)
	{
		return this;
	}
	
	default IDataMember capture(IContext context, IVariable variable)
	{
		return this;
	}
}
