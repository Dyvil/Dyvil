package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class Instruction implements IInstruction
{
	protected int opcode;
	
	public Instruction(int opcode)
	{
		this.opcode = opcode;
	}
	
	@Override
	public void resolve(MarkerList markers, InstructionList instructionList)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(this.opcode);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode));
	}
}
