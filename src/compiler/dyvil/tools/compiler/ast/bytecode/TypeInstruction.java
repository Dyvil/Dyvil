package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class TypeInstruction implements IInstruction, IInternalTyped
{
	private int		opcode;
	private String	type;
	
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
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void setInternalType(String desc)
	{
		this.type = desc;
	}
	
	@Override
	public String getInternalType()
	{
		return this.type;
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeTypeInsn(this.opcode, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.type);
	}
}
