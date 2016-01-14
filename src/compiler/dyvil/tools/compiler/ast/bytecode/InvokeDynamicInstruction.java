package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.asm.Handle;
import dyvil.tools.asm.util.Printer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

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
	public void resolve(MarkerList markers, InstructionList instructionList)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeInvokeDynamic(this.name, this.type, this.bsm, this.bsmArguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("INVOKEDYNAMIC ").append(this.name).append(':').append(this.type).append(' ');
		buffer.append(Printer.HANDLE_TAG[this.bsm.getTag()]).append(' ');
		buffer.append(this.bsm.getOwner()).append('.').append(this.bsm.getDesc()).append(':')
		      .append(this.bsm.getDesc());
		
		int len = this.bsmArguments.length;
		if (len > 0)
		{
			// TODO Clean this up...
			IValue.fromObject(this.bsmArguments[0]).toString(prefix, buffer);
			for (int i = 1; i < len; i++)
			{
				buffer.append(", ");
				IValue.fromObject(this.bsmArguments[i]).toString(prefix, buffer);
			}
		}
		else
		{
			buffer.append("{}");
		}
	}
}
