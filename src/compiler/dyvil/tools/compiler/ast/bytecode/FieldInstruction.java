package dyvil.tools.compiler.ast.bytecode;

import static dyvil.reflect.Opcodes.GETFIELD;
import static dyvil.reflect.Opcodes.GETSTATIC;
import static dyvil.reflect.Opcodes.PUTFIELD;
import static dyvil.reflect.Opcodes.PUTSTATIC;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class FieldInstruction implements IInstruction
{
	private int		opcode;
	private String	owner;
	private String	fieldName;
	
	private String	desc;
	private Object	type;
	
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
		this.type = ClassFormat.getFrameType(desc);
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	public void setType(Object type)
	{
		this.type = type;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		String s = ClassFormat.userToExtended(this.desc);
		
		switch (this.opcode)
		{
		case GETSTATIC:
			writer.writeGetStatic(this.owner, this.fieldName, s, this.type);
			return;
		case PUTSTATIC:
			writer.writePutStatic(this.owner, this.fieldName, s);
			return;
		case GETFIELD:
			writer.writeGetField(this.owner, this.fieldName, s, this.type);
			return;
		case PUTFIELD:
			writer.writePutField(this.owner, this.fieldName, s);
			return;
		}
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
