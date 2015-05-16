package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class TryCatchInstruction implements IInstruction
{
	private Label	start;
	private Label	end;
	private Label	handler;
	private String	type;
	
	public TryCatchInstruction(Label start, Label end, Label handler, String type)
	{
		this.start = start;
		this.end = end;
		this.handler = handler;
		this.type = type;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		writer.writeCatchBlock(this.start.target, this.end.target, this.handler.target, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("TRYCATCHBLOCK ").append(this.type).append(" [").append(this.start).append(' ').append(this.end).append(" => ").append(this.handler);
	}
}
