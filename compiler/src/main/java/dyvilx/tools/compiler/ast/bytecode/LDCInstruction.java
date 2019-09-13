package dyvilx.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class LDCInstruction implements IInstruction
{
	private Object argument;

	public LDCInstruction(Object value)
	{
		this.argument = value;
	}

	@Override
	public int getOpcode()
	{
		return Opcodes.LDC;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitLdcInsn(this.argument);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("LDC ").append(this.argument);
	}
}
