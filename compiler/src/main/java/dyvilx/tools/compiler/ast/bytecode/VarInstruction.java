package dyvilx.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public final class VarInstruction implements IInstruction
{
	private int opcode;
	private int index;
	
	public VarInstruction(int opcode, int index)
	{
		this.opcode = opcode;
		this.index = index;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	public int getIndex()
	{
		return this.index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitVarInsn(this.opcode, this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.index);
	}
}
