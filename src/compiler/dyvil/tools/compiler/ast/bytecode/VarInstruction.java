package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;

public class VarInstruction extends Instruction
{
	private int	index	= -1;
	
	public VarInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof Integer && this.index == -1)
		{
			this.index = ((Integer) arg).intValue();
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeVarInsn(this.opcode, this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.index);
	}
}
