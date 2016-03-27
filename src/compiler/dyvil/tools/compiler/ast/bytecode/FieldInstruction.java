package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public final class FieldInstruction implements IInstruction
{
	private int    opcode;
	private String owner;
	private String fieldName;
	private String desc;
	
	public FieldInstruction(int opcode)
	{
		this.opcode = opcode;
	}
	
	public FieldInstruction(int opcode, String owner, String name, String desc)
	{
		this.opcode = opcode;
		this.owner = owner;
		this.fieldName = name;
		this.desc = desc;
	}

	@Override
	public int getOpcode()
	{
		return this.opcode;
	}

	public String getOwner()
	{
		return this.owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getFieldName()
	{
		return this.fieldName;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	public String getDesc()
	{
		return this.desc;
	}

	public void setDesc(String desc)
	{
		this.desc = ClassFormat.userToExtended(desc);
	}
	
	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitFieldInsn(this.opcode, this.owner, this.fieldName, this.desc);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ');
		buffer.append(this.owner).append('.');
		buffer.append(this.fieldName).append(" : ");
		buffer.append(this.desc);
	}
}
