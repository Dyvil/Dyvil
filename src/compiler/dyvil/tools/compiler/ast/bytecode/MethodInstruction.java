package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MethodInstruction implements IInstruction
{
	private int		opcode;
	private String	owner;
	private String	name;
	private String	desc;
	private boolean	isInterface;
	
	private int		args;
	private IType	type;
	
	public MethodInstruction(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		this.opcode = opcode;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isInterface = isInterface;
		
		if (opcode != Opcodes.INVOKESTATIC)
		{
			this.args++;
		}
		this.args = org.objectweb.asm.Type.getArgumentsAndReturnSizes(desc);
		this.type = ClassFormat.internalToType(desc.substring(desc.lastIndexOf(')') + 1));
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeInvokeInsn(this.opcode, this.owner, this.name, this.desc, this.isInterface, this.args, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ');
		buffer.append(this.owner).append('.');
		buffer.append(this.name).append(':');
		buffer.append(this.desc);
	}
}
