package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class FrameInstruction implements IInstruction
{
	private int			type;
	private int			stackCount;
	private Object[]	stack;
	private int			localCount;
	private Object[]	locals;
	
	public FrameInstruction(int type, int stackCount, Object[] stack, int localCount, Object[] locals)
	{
		this.type = type;
		this.stackCount = stackCount;
		this.stack = stack;
		this.localCount = localCount;
		this.locals = locals;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeFrame(this.type, this.stackCount, this.stack, this.localCount, this.locals);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO type
		buffer.append("FRAME FULL ");
		if (this.stackCount > 0)
		{
			buffer.append("{ ");
			buffer.append(MethodWriterImpl.typeToString(this.stack[0]));
			for (int i = 1; i < this.stackCount; i++)
			{
				buffer.append(", ");
				buffer.append(MethodWriterImpl.typeToString(this.stack[i]));
			}
			buffer.append(" }");
		}
		else
		{
			buffer.append("{}");
		}
		
		buffer.append(", ");
		if (this.localCount > 0)
		{
			buffer.append("{ ");
			buffer.append(MethodWriterImpl.typeToString(this.locals[0]));
			for (int i = 1; i < this.localCount; i++)
			{
				buffer.append(", ");
				buffer.append(MethodWriterImpl.typeToString(this.locals[i]));
			}
			buffer.append(" }");
		}
		else
		{
			buffer.append("{}");
		}
	}
}
