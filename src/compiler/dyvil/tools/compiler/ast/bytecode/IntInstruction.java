package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;

public class IntInstruction extends Instruction
{
	private int	value	= Integer.MIN_VALUE;
	
	public IntInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof Integer && this.value == Integer.MIN_VALUE)
		{
			this.value = (Integer) arg;
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.visitIntInsn(this.opcode, this.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.value);
	}
}
