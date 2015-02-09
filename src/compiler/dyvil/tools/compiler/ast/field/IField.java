package dyvil.tools.compiler.ast.field;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IField extends IASTNode, IMember, IValued
{
	public default boolean isEnumConstant()
	{
		return false;
	}
	
	// Compilation
	
	public void write(ClassWriter writer);
	
	public void writeGet(MethodWriter writer, IValue instance);
	
	public void writeSet(MethodWriter writer, IValue instance, IValue value);
	
	public String getDescription();
	
	public String getSignature();
}
