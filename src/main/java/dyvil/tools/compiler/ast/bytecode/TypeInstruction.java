package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.ClassFormat;

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
		writer.visitTypeInsn(this.opcode, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.type);
	}
}
