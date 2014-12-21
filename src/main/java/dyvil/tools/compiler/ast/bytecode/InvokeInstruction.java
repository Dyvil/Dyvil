package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.ClassFormat;

public class InvokeInstruction extends Instruction
{
	private String			owner;
	private String			methodName;
	private String			desc;
	
	public InvokeInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String)
		{
			if (this.owner == null)
			{
				this.owner = ClassFormat.packageToInternal((String) arg);
				return true;
			}
			else if (this.methodName == null)
			{
				this.methodName = (String) arg;
				return true;
			}
			else if (this.desc == null)
			{
				this.desc = (String) arg;
				return true;
			}
		}
		return false;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void write(MethodWriter writer)
	{
		writer.visitMethodInsn(this.opcode, this.owner, this.methodName, this.desc);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		buffer.append('"').append(this.owner);
		buffer.append("\", \"").append(this.methodName);
		buffer.append("\", \"").append(this.desc).append('"');
	}
}
