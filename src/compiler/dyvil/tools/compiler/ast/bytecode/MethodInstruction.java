package dyvil.tools.compiler.ast.bytecode;

import org.objectweb.asm.Type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MethodInstruction implements IInstruction
{
	private int			opcode;
	private String		owner;
	private String		name;
	private String		desc;
	private boolean		isInterface;
	
	private int			argsCount;
	private String[]	args;
	private String		returnDesc;
	private Object		returnType;
	
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
		
		this.argsCount = (org.objectweb.asm.Type.getArgumentsAndReturnSizes(desc) >> 2) - 1;
		this.returnType = ClassFormat.getFrameType(desc.substring(desc.lastIndexOf(')') + 1));
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setMethodName(String name)
	{
		this.name = name;
	}
	
	public void setReturnDesc(String returnDesc)
	{
		this.returnDesc = returnDesc;
	}
	
	public void setReturnType(Object returnType)
	{
		this.returnType = returnType;
	}
	
	public void setInterface(boolean isInterface)
	{
		this.isInterface = isInterface;
	}
	
	public void addArgument(String desc)
	{
		if (this.args == null)
		{
			this.args = new String[3];
			this.argsCount = 1;
			this.args[0] = desc;
			return;
		}
		
		int index = this.argsCount++;
		if (index >= this.args.length)
		{
			String[] temp = new String[this.argsCount];
			System.arraycopy(args, 0, temp, 0, args.length);
			this.args = temp;
		}
		this.args[index] = desc;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		this.getDesc();
		writer.writeInvokeInsn(this.opcode, this.owner, this.name, this.desc, this.isInterface, this.argsCount, this.returnType);
	}
	
	private void getDesc()
	{
		if (this.desc == null)
		{
			StringBuilder builder = new StringBuilder();
			builder.append('(');
			for (int i = 0; i < this.argsCount; i++)
			{
				builder.append(ClassFormat.userToExtended(this.args[i]));
			}
			builder.append(')');
			builder.append(ClassFormat.userToExtended(this.returnDesc));
			this.desc = builder.toString();
		}
	}
	
	private void getArgs()
	{
		if (this.args == null)
		{
			this.args = new String[this.argsCount];
			Type[] types = Type.getArgumentTypes(this.desc);
			for (int i = 0; i < this.argsCount; i++)
			{
				this.args[i] = types[i].getClassName();
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ');
		buffer.append(this.owner).append('.');
		buffer.append(this.name);
		
		if (this.argsCount > 0)
		{
			this.getArgs();
			
			buffer.append('(');
			buffer.append(this.args[0]);
			for (int i = 1; i < this.argsCount; i++)
			{
				buffer.append(", ").append(this.args[i]);
			}
			buffer.append(')');
		}
		else
		{
			buffer.append("()");
		}
		buffer.append(" : ").append(this.returnDesc);
	}
}
