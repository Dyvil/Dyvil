package dyvilx.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class IntInstruction implements IInstruction
{
	private int opcode;
	private int operand;
	
	public IntInstruction(int opcode, int operand)
	{
		this.opcode = opcode;
		this.operand = operand;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	public int getOperand()
	{
		return this.operand;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitIntInsn(this.opcode, this.operand);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.operand);
	}
}
