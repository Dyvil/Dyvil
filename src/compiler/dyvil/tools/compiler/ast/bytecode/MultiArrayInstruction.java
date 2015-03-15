package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MultiArrayInstruction implements IInstruction
{
	private String	type;
	private int		dims;
	
	public MultiArrayInstruction(String type, int dims)
	{
		this.type = type;
		this.dims = dims;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeNewArray(this.type, this.dims);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("MULTIANEWARRAY ").append(this.type);
	}
}
