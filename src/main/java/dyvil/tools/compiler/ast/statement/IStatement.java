package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.ast.value.IValue;

public interface IStatement extends IValue
{
	@Override
	public default void writeJump(MethodVisitor visitor, Label label) { }
}
