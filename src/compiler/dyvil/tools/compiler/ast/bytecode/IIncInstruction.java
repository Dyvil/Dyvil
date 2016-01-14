package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

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
	public void resolve(MarkerList markers, InstructionList instructionList)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeIINC(this.index, this.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("IINC ").append(this.value);
	}
}
