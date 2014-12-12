package dyvil.tools.compiler.bytecode;

import jdk.internal.org.objectweb.asm.MethodVisitor;

public class MethodWriter extends MethodVisitor
{
	public MethodWriter(int mode, MethodVisitor mv)
	{
		super(mode, mv);
	}
}
