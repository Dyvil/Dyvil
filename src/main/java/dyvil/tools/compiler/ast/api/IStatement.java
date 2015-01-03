package dyvil.tools.compiler.ast.api;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface IStatement extends IValue
{
	@Override
	public default void writeJump(MethodWriter writer, Label label)
	{
	}
}
