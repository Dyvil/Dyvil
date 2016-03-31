package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class MultiArrayInstruction implements IInstruction
{
	private String type;
	private int    dims;
	
	public MultiArrayInstruction(String type, int dims)
	{
		this.type = type;
		this.dims = dims;
	}

	@Override
	public int getOpcode()
	{
		return Opcodes.MULTIANEWARRAY;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitMultiANewArrayInsn(this.type, this.dims);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("MULTIANEWARRAY ").append(this.type);
	}
}
