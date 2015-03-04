package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IField extends IASTNode, IMember, IClassCompilable, IValued
{
	public default boolean isEnumConstant()
	{
		return false;
	}
	
	// Compilation
	
	public void writeGet(MethodWriter writer, IValue instance);
	
	public void writeSet(MethodWriter writer, IValue instance, IValue value);
	
	public String getDescription();
	
	public String getSignature();
}
