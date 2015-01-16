package dyvil.tools.compiler.ast.bytecode;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;

public class ConstantInstruction extends Instruction
{
	private IValue	argument;
	
	public ConstantInstruction(String name)
	{
		super(Opcodes.LDC, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (this.argument == null)
		{
			this.argument = IValue.fromObject(arg);
			return true;
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		this.argument.writeExpression(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		this.argument.toString("", buffer);
	}
}
