package dyvil.tools.compiler.ast.type;

import jdk.internal.org.objectweb.asm.Opcodes;

final class UnknownType extends Type
{
	@Override
	public Object getFrameType()
	{
		return Opcodes.NULL;
	}
	
	@Override
	public String toString()
	{
		return "unknown";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("unknown");
	}
}
