package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public final class MethodInstruction implements IInstruction
{
	private int     opcode;
	private String  owner;
	private String  name;
	private String  desc;
	private boolean isInterface;
	
	public MethodInstruction(int opcode)
	{
		this.opcode = opcode;
	}
	
	public MethodInstruction(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		this.opcode = opcode;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isInterface = isInterface;
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

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDesc()
	{
		return this.desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitMethodInsn(this.opcode, this.owner, this.name, this.desc, this.isInterface);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ');
		buffer.append(this.owner).append('.');
		buffer.append(this.name).append(this.desc);
	}
}
