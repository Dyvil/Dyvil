package dyvilx.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class Instruction implements IInstruction
{
	protected int opcode;
	
	public Instruction(int opcode)
	{
		this.opcode = opcode;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitInsn(this.opcode);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode));
	}
}
