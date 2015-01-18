package dyvil.tools.compiler.ast.bytecode;

import static dyvil.reflect.Opcodes.GETFIELD;
import static dyvil.reflect.Opcodes.GETSTATIC;
import static dyvil.reflect.Opcodes.PUTFIELD;
import static dyvil.reflect.Opcodes.PUTSTATIC;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;

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
			writer.visitGetStatic(this.owner, this.fieldName, this.desc, this.type);
		case PUTSTATIC:
			writer.visitPutStatic(this.owner, this.fieldName, this.desc);
		case GETFIELD:
			writer.visitGetField(this.owner, this.fieldName, this.desc, this.type);
		case PUTFIELD:
			writer.visitPutField(this.owner, this.fieldName, this.desc);
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
