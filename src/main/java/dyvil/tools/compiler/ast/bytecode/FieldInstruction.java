package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.ClassFormat;

public class FieldInstruction extends Instruction
{
	private String			owner;
	private String			fieldName;
	private String desc;
	
	public FieldInstruction(int opcode, String name)
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
			else if (this.fieldName == null)
			{
				this.fieldName = (String) arg;
				return true;
			}
			else if (this.desc == null)
			{
				this.desc = ClassFormat.typeToInternal((String) arg);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.visitFieldInsn(this.opcode, this.owner, this.fieldName, this.desc);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		buffer.append('"').append(this.owner);
		buffer.append("\", \"").append(this.fieldName);
		buffer.append("\", \"").append(this.desc).append('"');
	}
}
