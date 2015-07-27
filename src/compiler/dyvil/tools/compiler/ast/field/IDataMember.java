package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IDataMember extends IMember, IValued, IAccessible
{
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context);
	
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue);
	
	public default boolean isEnumConstant()
	{
		return false;
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
}
