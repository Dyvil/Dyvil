package dyvil.tools.compiler.bytecode;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

public class MethodWriter extends MethodVisitor
{
	private int locals;
	
	public MethodWriter(int mode, MethodVisitor mv)
	{
		super(mode, mv);
	}
	
	@Override
	public void visitParameter(String desc, int index)
	{
		this.locals++;
		this.mv.visitParameter(desc, index);
	}
	
	@Override
	public void visitEnd()
	{
		this.mv.visitInsn(Opcodes.RETURN);
		this.mv.visitMaxs(10, this.locals);
		this.mv.visitEnd();
	}
}
