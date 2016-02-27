package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.asm.Handle;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.asm.util.Printer;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class InvokeDynamicInstruction implements IInstruction
{
	private String   name;
	private String   type;
	private Handle   bsm;
	private Object[] bsmArguments;
	
	public InvokeDynamicInstruction(String name, String type, Handle bsm, Object[] bsmArguments)
	{
		this.name = name;
		this.type = type;
		this.bsm = bsm;
		this.bsmArguments = bsmArguments;
	}
	
	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitInvokeDynamicInsn(this.name, this.type, this.bsm, this.bsmArguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("INVOKEDYNAMIC ").append(this.name).append(':').append(this.type).append(' ');
		buffer.append(Printer.HANDLE_TAG[this.bsm.getTag()]).append(' ');
		buffer.append(this.bsm.getOwner()).append('.').append(this.bsm.getDesc()).append(':')
		      .append(this.bsm.getDesc());
		
		final int len = this.bsmArguments.length;
		if (len > 0)
		{
			buffer.append("{ ");
			buffer.append(this.bsmArguments[0]);
			for (int i = 1; i < len; i++)
			{
				buffer.append(", ");
				buffer.append(this.bsmArguments[i]);
			}
			buffer.append(" }");
		}
		else
		{
			buffer.append("{}");
		}
	}
}
