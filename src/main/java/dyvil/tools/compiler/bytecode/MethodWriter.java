package dyvil.tools.compiler.bytecode;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.type.Type;

public class MethodWriter extends MethodVisitor
{
	private int		locals;
	private boolean	hasReturn;
	
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
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		this.locals++;
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}
	
	@Override
	public void visitInsn(int insn)
	{
		this.hasReturn = insn == Opcodes.RETURN || insn == Opcodes.ARETURN || insn == Opcodes.IRETURN || insn == Opcodes.LRETURN || insn == Opcodes.FRETURN || insn == Opcodes.DRETURN;
		this.mv.visitInsn(insn);
	}
	
	public void visitEnd(Type type)
	{
		if (!this.hasReturn)
		{
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(10, this.locals);
		this.mv.visitEnd();
	}
}
