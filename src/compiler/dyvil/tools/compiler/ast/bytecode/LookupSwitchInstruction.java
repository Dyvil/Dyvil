package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;

public class LookupSwitchInstruction implements IInstruction
{
	private Label   defaultHandler;
	private int[]   keys;
	private Label[] handlers;
	
	public LookupSwitchInstruction(Label defaultHandler, int[] keys, Label[] handlers)
	{
		this.defaultHandler = defaultHandler;
		this.keys = keys;
		this.handlers = handlers;
	}

	@Override
	public int getOpcode()
	{
		return Opcodes.LOOKUPSWITCH;
	}

	@Override
	public void write(MethodVisitor writer) throws BytecodeException
	{
		writer.visitLookupSwitchInsn(this.defaultHandler, this.keys, this.handlers);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("LOOKUPSWITCH [");

		int len = Math.min(this.keys.length, this.handlers.length);
		String switchPrefix = Formatting.getIndent("bytecode.switch.indent", prefix);
		
		for (int i = 0; i < len; i++)
		{
			buffer.append('\n').append(switchPrefix).append(this.keys[i]).append(": ").append(this.handlers[i]);
		}

		buffer.append('\n').append(switchPrefix).append("default: ").append(this.defaultHandler);

		buffer.append('\n').append(prefix).append(']');
	}
}
