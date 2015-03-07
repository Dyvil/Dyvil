package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;

public class TypeInstruction extends Instruction
{
	private IType	type;
	
	public TypeInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String && this.type == null)
		{
			this.type = ClassFormat.internalToType((String) arg);
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeTypeInsn(this.opcode, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.type);
	}
}
