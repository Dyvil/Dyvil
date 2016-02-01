package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IInstruction
{
	void write(MethodVisitor visitor) throws BytecodeException;

	void toString(String prefix, StringBuilder buffer);
}
