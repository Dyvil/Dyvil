package dyvil.tools.compiler.backend.visitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.type.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;

public final class SimpleMethodVisitor implements MethodVisitor
{
	private final ICallableMember method;
	
	public SimpleMethodVisitor(ICallableMember method)
	{
		this.method = method;
	}
	
	@Override
	public void visitParameter(String name, int index)
	{
		this.method.getParameter(index).setName(Name.getQualified(name));
	}
	
	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			if (index != 0 && index <= this.method.parameterCount())
			{
				this.method.getParameter(index - 1).setName(Name.getQualified(name));
			}
			return;
		}
		
		if (index < this.method.parameterCount())
		{
			this.method.getParameter(index).setName(Name.getQualified(name));
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String internal = ClassFormat.extendedToInternal(type);
		if (this.method.addRawAnnotation(internal))
		{
			Annotation annotation = new Annotation(new InternalType(internal));
			return new AnnotationVisitorImpl(this.method, annotation);
		}
		return null;
	}
	
	@Override
	public AnnotationVisitor visitAnnotationDefault()
	{
		return null;
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{
	}
	
	@Override
	public void visitCode()
	{
	}
	
	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack)
	{
	}
	
	@Override
	public void visitInsn(int opcode)
	{
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand)
	{
	}
	
	@Override
	public void visitVarInsn(int opcode, int var)
	{
	}
	
	@Override
	public void visitTypeInsn(int opcode, String type)
	{
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc)
	{
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
	{
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs)
	{
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
	}
	
	@Override
	public void visitLabel(Label label)
	{
	}
	
	@Override
	public void visitLdcInsn(Object cst)
	{
	}
	
	@Override
	public void visitIincInsn(int var, int increment)
	{
	}
	
	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels)
	{
	}
	
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
	}
	
	@Override
	public void visitMultiANewArrayInsn(String desc, int dims)
	{
	}
	
	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
	{
	}
	
	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitLineNumber(int line, Label start)
	{
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals)
	{
	}
	
	@Override
	public void visitEnd()
	{
	}
}
