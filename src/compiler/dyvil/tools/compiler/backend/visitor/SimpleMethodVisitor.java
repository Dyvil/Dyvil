package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.parsing.Name;

public final class SimpleMethodVisitor implements MethodVisitor
{
	private final IExternalCallableMember method;
	
	public SimpleMethodVisitor(IExternalCallableMember method)
	{
		this.method = method;
	}
	
	private int parameterIndex;
	
	@Override
	public void visitParameter(String name, int modifiers)
	{
		final IParameter parameter = this.method.getParameter_(this.parameterIndex);
		if (parameter == null)
		{
			return;
		}

		this.parameterIndex++;
		parameter.setName(Name.getQualified(name));

		if (modifiers != 0)
		{
			parameter.getModifiers().addIntModifier(modifiers);
		}
	}
	
	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String type, boolean visible)
	{
		IParameter param = this.method.getParameter_(parameter);
		if (AnnotationUtil.DYVIL_MODIFIERS.equals(type))
		{
			return new ModifierVisitor(param.getModifiers());
		}

		String internal = ClassFormat.extendedToInternal(type);
		return param.visitAnnotation(internal);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		if (AnnotationUtil.DYVIL_MODIFIERS.equals(type))
		{
			return new ModifierVisitor(this.method.getModifiers());
		}
		if (AnnotationUtil.RECEIVER_TYPE.equals(type))
		{
			return new ReceiverTypeVisitor((IMethod) this.method);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (this.method.addRawAnnotation(internal, null))
		{
			Annotation annotation = new Annotation(new InternalType(internal));
			return new AnnotationReader(this.method, annotation);
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
		return this.method.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{
	}
	
	@Override
	public boolean visitCode()
	{
		return true;
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
