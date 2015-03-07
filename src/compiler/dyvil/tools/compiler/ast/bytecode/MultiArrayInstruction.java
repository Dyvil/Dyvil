package dyvil.tools.compiler.ast.bytecode;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;

public class MultiArrayInstruction extends Instruction
{
	private String	type;
	private int		dims	= Integer.MIN_VALUE;
	
	public MultiArrayInstruction(String name)
	{
		super(Opcodes.MULTIANEWARRAY, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String && this.type == null)
		{
			this.type = ClassFormat.packageToInternal((String) arg);
			return true;
		}
		else if (arg instanceof Integer && this.dims == Integer.MIN_VALUE)
		{
			this.dims = (Integer) arg;
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeNewArray(this.type, this.dims);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.type);
	}
}
