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
	public IValue getValue();
	
	@Override
	public void setValue(IValue value);
	
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context);
	
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue);
	
	public default boolean isEnumConstant()
	{
		return (this.getModifiers() & Modifiers.ENUM) != 0;
	}
	
	public default IClass getTheClass()
	{
		return null;
	}
	
	public boolean isField();
	
	public boolean isVariable();
	
	// Compilation
	
	@Override
	public default void writeGet(MethodWriter writer) throws BytecodeException
	{
		this.writeGet(writer, null, 0);
	}
	
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException;
	
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException;
	
	public String getDescription();
	
	public String getSignature();
	
	public default IDataMember capture(IContext context)
	{
		return this;
	}
	
	public default IDataMember capture(IContext context, IVariable variable)
	{
		return this;
	}
}
