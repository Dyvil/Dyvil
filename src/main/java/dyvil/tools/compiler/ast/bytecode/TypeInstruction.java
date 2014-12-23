package dyvil.tools.compiler.ast.bytecode;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.ClassFormat;

public class TypeInstruction extends Instruction
{
	private String	type;
	
	public TypeInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String && this.type == null)
		{
			this.type = ClassFormat.packageToInternal((String) arg);
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		if (this.opcode == Opcodes.NEWARRAY || this.opcode == Opcodes.ANEWARRAY)
		{
			switch (this.type)
			{
			case "B":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
				return;
			case "S":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
				return;
			case "C":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_CHAR);
				return;
			case "I":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
				return;
			case "L":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
				return;
			case "F":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
				return;
			case "D":
				writer.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
				return;
			}
		}
		writer.visitTypeInsn(this.opcode, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.type);
	}
}
