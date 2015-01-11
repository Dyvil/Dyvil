package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.ClassFormat;
import static dyvil.reflect.Opcodes.*;

public class FieldInstruction extends Instruction
{
	private String	owner;
	private String	fieldName;
	private String	desc;
	private IType	type;
	
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
				this.desc = ClassFormat.userToInternal((String) arg);
				if (this.opcode == GETSTATIC || this.opcode == GETFIELD)
				{
					this.type = ClassFormat.internalToType(this.desc);
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		switch (this.opcode)
		{
		case GETSTATIC:
			writer.visitGetStatic(owner, fieldName, desc, type);
		case PUTSTATIC:
			writer.visitPutStatic(owner, fieldName, desc);
		case GETFIELD:
			writer.visitGetField(owner, fieldName, desc, type);
		case PUTFIELD:
			writer.visitPutField(owner, fieldName, desc);
		}
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
