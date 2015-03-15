package dyvil.tools.compiler.ast.bytecode;

import static dyvil.reflect.Opcodes.*;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

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
		writer.writeInsn(this.opcode);
	}
	
	public void resolve(MarkerList markers, Bytecode bytecode)
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
		else if (Opcodes.isInvokeOpcode(opcode))
		{
			return new InvokeInstruction(opcode, name);
		}
		else if (Opcodes.isFieldOpcode(opcode))
		{
			return new FieldInstruction(opcode, name);
		}
		else if (Opcodes.isLoadOpcode(opcode) || Opcodes.isStoreOpcode(opcode))
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
		else if (Opcodes.isJumpOpcode(opcode))
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
