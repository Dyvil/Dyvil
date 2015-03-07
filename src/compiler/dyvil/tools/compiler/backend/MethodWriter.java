package dyvil.tools.compiler.backend;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.type.IType;

public interface MethodWriter
{
	Integer	TOP				= jdk.internal.org.objectweb.asm.Opcodes.TOP;
	Integer	INT				= jdk.internal.org.objectweb.asm.Opcodes.INTEGER;
	Integer	LONG			= jdk.internal.org.objectweb.asm.Opcodes.LONG;
	Integer	FLOAT			= jdk.internal.org.objectweb.asm.Opcodes.FLOAT;
	Integer	DOUBLE			= jdk.internal.org.objectweb.asm.Opcodes.DOUBLE;
	
	public void setConstructor(IType type);
	
	// Annotations
	
	public AnnotationVisitor visitAnnotation(String type, boolean visible);
	
	public AnnotationVisitor visitParameterAnnotation(int index, String type, boolean visible);
	
	// Code
	
	public void visitCode();
	
	// Stack
	
	public void set(Object type);
	
	public void set(IType type);
	
	public void push(Object type);
	
	public void push(IType type);
	
	public void pop();
	
	public void pop(int count);
	
	public Object peek();
	
	// Parameters
	
	public int visitParameter(String name, Object type);
	
	public int visitParameter(String name, IType type);
	
	// Local Variables
	
	public int addLocal(Object type);
	
	public int addLocal(IType type);
	
	public void removeLocals(int count);
	
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index);
	
	public void visitLocalVariable(String name, IType type, Label start, Label end, int index);
	
	// Constants
	
	public void visitLdcInsn(int value);
	
	public void visitLdcInsn(long value);
	
	public void visitLdcInsn(float value);
	
	public void visitLdcInsn(double value);
	
	public void visitLdcInsn(String value);
	
	public void visitLdcInsn(Object value);
	
	// Labels
	
	public void visitLabel(Label label);
	
	public void visitLabel2(Label label);
	
	// Instructions
	
	public void visitInsn(int opcode);
	
	public void visitJumpInsn(int opcode, Label label);
	
	public void visitJumpInsn2(int opcode, Label label);
	
	public void visitTypeInsn(int opcode, String type);
	
	public void visitTypeInsn(int opcode, IType type);
	
	public void visitMultiANewArrayInsn(String type, int dims);
	
	public void visitMultiANewArrayInsn(IType type, int dims);
	
	public void visitVarInsn(int opcode, int var);
	
	public void visitIincInsn(int var, int value);
	
	// Field Instructions
	
	public void visitGetStatic(String owner, String name, String desc, IType type);
	
	public void visitPutStatic(String owner, String name, String desc);
	
	public void visitGetField(String owner, String name, String desc, IType type);
	
	public void visitPutField(String owner, String name, String desc);
	
	// Invoke Instructions
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, int args, Object returnType);
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, int args, IType returnType);
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, Object returnType);
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, IType returnType);
	
	public void visitInvokeDynamicInsn(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs);
	
	public void visitInvokeDynamicInsn(String name, String desc, int args, IType returnType, Handle bsm, Object... bsmArgs);
	
	// End
	
	public void visitEnd();
	
	public void visitEnd(IType type);
}
