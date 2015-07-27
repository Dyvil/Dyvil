package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class TableSwitchInstruction implements IInstruction
{
	private int		start;
	private int		end;
	private Label	defaultHandler;
	private Label[]	handlers;
	
	public TableSwitchInstruction(int start, int end, Label defaultHandler, Label[] handlers)
	{
		this.start = start;
		this.end = end;
		this.defaultHandler = defaultHandler;
		this.handlers = handlers;
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
	}
	
	@Override
	public void write(MethodWriter writer) throws BytecodeException
	{
		int len = this.handlers.length;
		dyvil.tools.asm.Label[] labels = new dyvil.tools.asm.Label[len];
		for (int i = 0; i < len; i++)
		{
			labels[i] = this.handlers[i].target;
		}
		writer.writeTableSwitch(this.defaultHandler.target, this.start, this.end, labels);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("TABLESWITCH [");
		int len = Math.min(this.end - this.start + 1, this.handlers.length);
		String prefix1 = prefix + Formatting.Method.indent;
		
		for (int i = 0; i < len; i++)
		{
			buffer.append('\n').append(prefix1).append(this.start + i).append(": ").append(this.handlers[i]);
		}
		buffer.append('\n').append(prefix1).append("default: ").append(this.defaultHandler);
		buffer.append('\n').append(prefix).append(']');
	}
}
