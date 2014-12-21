package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface IStatement extends IValue
{
	@Override
	public default void writeJump(MethodWriter writer, Label label)
	{
	}
}
