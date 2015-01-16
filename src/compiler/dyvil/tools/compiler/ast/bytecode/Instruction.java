package dyvil.tools.compiler.ast.bytecode;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.ClassFormat;

public class Instruction extends ASTNode
{
	protected int		opcode;
	protected String	name;
	
	protected Label		label;
	
	public Instruction(int opcode, String name)
	{
		this.opcode = opcode;
		this.name = name;
	}
	
	public int getOpcode()
	{
		return this.opcode;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public boolean addArgument(Object arg)
	{
		return false;
	}
	
	public void write(MethodWriter writer)
	{
		writer.visitInsn(this.opcode);
	}
	
	public void resolve(List<Marker> markers, Bytecode bytecode)
	{
	}
	
	public static Instruction parse(String name)
	{
		if ("local".equalsIgnoreCase(name))
		{
			return new LocalInstruction(name);
		}
		
		int opcode = ClassFormat.parseOpcode(name);
		if (opcode == -1)
		{
			return null;
		}
		else if (opcode == LDC)
		{
			return new ConstantInstruction(name);
		}
		else if (opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE || opcode == INVOKESPECIAL || opcode == INVOKESTATIC)
		{
			return new InvokeInstruction(opcode, name);
		}
		else if (opcode == GETFIELD || opcode == PUTFIELD || opcode == GETSTATIC || opcode == PUTSTATIC)
		{
			return new FieldInstruction(opcode, name);
		}
		else if (opcode == ALOAD || opcode == ILOAD || opcode == LLOAD || opcode == FLOAD || opcode == DLOAD || opcode == ASTORE || opcode == ISTORE || opcode == LSTORE || opcode == FSTORE || opcode == DSTORE || opcode == RET)
		{
			return new VarInstruction(opcode, name);
		}
		else if (opcode == BIPUSH || opcode == SIPUSH)
		{
			return new IntInstruction(opcode, name);
		}
		else if (opcode == NEW || opcode == NEWARRAY || opcode == ANEWARRAY || opcode == CHECKCAST || opcode == INSTANCEOF)
		{
			return new TypeInstruction(opcode, name);
		}
		else if (opcode == MULTIANEWARRAY)
		{
			return new MultiArrayInstruction(name);
		}
		else if (opcode == IINC)
		{
			return new IIncInstruction(opcode, name);
		}
		else if (opcode >= IFEQ && opcode <= JSR || opcode == IFNULL || opcode == IFNONNULL)
		{
			return new JumpInstruction(opcode, name);
		}
		return new Instruction(opcode, name);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
	}
}
