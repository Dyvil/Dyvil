package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.marker.MarkerList;

public class LookupSwitchInstruction implements IInstruction
{
	private Label	defaultHandler;
	private int[]	keys;
	private Label[]	handlers;
	
	public LookupSwitchInstruction(Label defaultHandler, int[] keys, Label[] handlers)
	{
		this.defaultHandler = defaultHandler;
		this.keys = keys;
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
		writer.writeLookupSwitch(this.defaultHandler.target, this.keys, labels);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("LOOKUPSWITCH [");
		int len = Math.min(this.keys.length, this.handlers.length);
		String prefix1 = prefix + Formatting.Method.indent;
		
		for (int i = 0; i < len; i++)
		{
			buffer.append('\n').append(prefix1).append(this.keys[i]).append(": ").append(this.handlers[i]);
		}
		buffer.append('\n').append(prefix1).append("default: ").append(this.defaultHandler);
		buffer.append('\n').append(prefix).append(']');
	}
}
