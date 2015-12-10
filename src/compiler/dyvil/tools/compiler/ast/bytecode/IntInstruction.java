package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

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
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		switch (this.opcode)
		{
		case Opcodes.SIPUSH:
		case Opcodes.BIPUSH:
			writer.writeLDC(this.operand);
			return;
		case Opcodes.NEWARRAY:
			writer.writeIntInsn(this.opcode, this.operand);
			return;
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.operand);
	}
}
