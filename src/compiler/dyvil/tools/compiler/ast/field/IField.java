package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IField extends IASTNode, IMember, IClassCompilable, IValued
{
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance);
	
	public IValue checkAssign(MarkerList markers, ICodePosition position, IValue instance, IValue newValue);
	
	public default boolean isEnumConstant()
	{
		return false;
	}
	
	public default boolean isField()
	{
		return false;
	}
	
	public default boolean isVariable()
	{
		return true;
	}
	
	// Compilation
	
	public void writeGet(MethodWriter writer, IValue instance);
	
	public void writeSet(MethodWriter writer, IValue instance, IValue value);
	
	public String getDescription();
	
	public String getSignature();
}
