package dyvilx.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public final class JumpInstruction implements IInstruction
{
	private int   opcode;
	private Label target;
	
	public JumpInstruction(int opcode, Label target)
	{
		this.opcode = opcode;
		this.target = target;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitJumpInsn(this.opcode, this.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.target);
	}
}
