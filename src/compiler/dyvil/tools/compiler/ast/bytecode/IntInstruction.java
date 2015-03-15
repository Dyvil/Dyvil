package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class IntInstruction implements IInstruction
{
	private int opcode;
	private int	operand;
	
	public IntInstruction(int opcode, int operand)
	{
		this.opcode = opcode;
		this.operand = operand;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		switch (this.opcode)
		{
		case Opcodes.SIPUSH:
		case Opcodes.BIPUSH:
			writer.writeLDC(this.operand);
			return;
		case Opcodes.NEWARRAY:
			switch (this.operand)
			{
			case org.objectweb.asm.Opcodes.T_BOOLEAN:
			case org.objectweb.asm.Opcodes.T_BYTE:
			case org.objectweb.asm.Opcodes.T_SHORT:
			case org.objectweb.asm.Opcodes.T_CHAR:
			case org.objectweb.asm.Opcodes.T_INT:
				writer.writeTypeInsn(Opcodes.NEWARRAY, Type.INT);
				return;
			case org.objectweb.asm.Opcodes.T_LONG:
				writer.writeTypeInsn(Opcodes.NEWARRAY, Type.LONG);
				return;
			case org.objectweb.asm.Opcodes.T_FLOAT:
				writer.writeTypeInsn(Opcodes.NEWARRAY, Type.FLOAT);
				return;
			case org.objectweb.asm.Opcodes.T_DOUBLE:
				writer.writeTypeInsn(Opcodes.NEWARRAY, Type.DOUBLE);
				return;
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.operand);
	}
}
