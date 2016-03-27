package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class TypeInstruction implements IInstruction
{
	private int    opcode;
	private String type;
	
	public TypeInstruction(int opcode)
	{
		this.opcode = opcode;
	}
	
	public TypeInstruction(int opcode, String type)
	{
		this.opcode = opcode;
		this.type = type;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitTypeInsn(this.opcode, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.type);
	}
}
