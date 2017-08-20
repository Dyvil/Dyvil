package dyvilx.tools.compiler.ast.bytecode;

import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface IInstruction
{
	int getOpcode();

	void write(MethodVisitor visitor) throws BytecodeException;

	void toString(String prefix, StringBuilder buffer);
}
