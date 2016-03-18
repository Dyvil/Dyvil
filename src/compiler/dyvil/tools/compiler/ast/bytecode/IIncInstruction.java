package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public final class IIncInstruction implements IInstruction
{
	private int index;
	private int value;
	
	public IIncInstruction(int index, int value)
	{
		this.index = index;
		this.value = value;
	}

	@Override
	public int getOpcode()
	{
		return Opcodes.IINC;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitIincInsn(this.index, this.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("IINC ").append(this.value);
	}
}
