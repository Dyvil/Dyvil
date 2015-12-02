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
	
	void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException;
	
	void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException;
	
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
