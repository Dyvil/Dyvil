package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

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
	public void resolve(MarkerList markers, InstructionList instructionList)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(this.opcode, this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.index);
	}
}
