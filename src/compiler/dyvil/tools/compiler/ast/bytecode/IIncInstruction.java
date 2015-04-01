package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class IIncInstruction implements IInstruction
{
	private int	index;
	private int	value;
	
	public IIncInstruction(int index, int value)
	{
		this.index = index;
		this.value = value;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeIINC(this.index + writer.inlineOffset(), this.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("IINC ").append(this.value);
	}
}
