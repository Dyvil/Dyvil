package dyvilx.tools.compiler.ast.bytecode;

import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class TryCatchInstruction implements IInstruction
{
	private Label  start;
	private Label  end;
	private Label  handler;
	private String type;
	
	public TryCatchInstruction(Label start, Label end, Label handler, String type)
	{
		this.start = start;
		this.end = end;
		this.handler = handler;
		this.type = type;
	}

	@Override
	public int getOpcode()
	{
		return -1;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitTryCatchBlock(this.start, this.end, this.handler, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("TRYCATCHBLOCK ").append(this.type).append(" [").append(this.start).append(' ').append(this.end)
		      .append(" => ").append(this.handler);
	}
}
