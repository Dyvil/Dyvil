package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.asm.Label;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;

public class TableSwitchInstruction implements IInstruction
{
	private int     start;
	private int     end;
	private Label   defaultHandler;
	private Label[] handlers;
	
	public TableSwitchInstruction(int start, int end, Label defaultHandler, Label[] handlers)
	{
		this.start = start;
		this.end = end;
		this.defaultHandler = defaultHandler;
		this.handlers = handlers;
	}

	@Override
	public int getOpcode()
	{
		return Opcodes.TABLESWITCH;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitTableSwitchInsn(this.start, this.end, this.defaultHandler, this.handlers);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("TABLESWITCH [");

		int len = Math.min(this.end - this.start + 1, this.handlers.length);
		String switchPrefix = Formatting.getIndent("bytecode.switch.indent", prefix);
		
		for (int i = 0; i < len; i++)
		{
			buffer.append('\n').append(switchPrefix).append(this.start + i).append(": ").append(this.handlers[i]);
		}

		buffer.append('\n').append(switchPrefix).append("default: ").append(this.defaultHandler);
		buffer.append('\n').append(prefix).append(']');
	}
}
