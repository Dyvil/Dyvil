package dyvilx.tools.compiler.backend.method;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.*;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface MethodWriter extends AnnotatableVisitor, TypeAnnotatableVisitor, MethodVisitor
{
	Frame getFrame();

	void setLocalType(int index, Object type);

	void setHasReturn(boolean hasReturn);

	boolean hasReturn();

	// Annotations

	@Override
	AnnotationVisitor visitAnnotation(String type, boolean visible);

	@Override
	AnnotationVisitor visitParameterAnnotation(int index, String type, boolean visible);

	@Override
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	@Override
	AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	@Override
	AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);

	@Override
	AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible);

	@Override
	AnnotationVisitor visitAnnotationDefault();

	@Override
	void visitAttribute(Attribute attr);

	// Code

	@Override
	default void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack)
	{
	}

	@Override
	default void visitMaxs(int maxStack, int maxLocals)
	{
	}

	@Override
	boolean visitCode();

	// Parameters

	int visitParameter(int index, String name, IType type, int access);

	@Override
	void visitParameter(String name, int access);

	// Local Variables

	int localCount();

	void resetLocals(int count);

	@Override
	void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index);

	// Constants

	void visitLdcInsn(int value);

	void visitLdcInsn(long value);

	void visitLdcInsn(float value);

	void visitLdcInsn(double value);

	void visitLdcInsn(String value);

	void visitLdcInsn(Type value);

	@Override
	void visitLdcInsn(Object cst);

	// Labels

	@Override
	void visitLabel(Label label);

	void visitTargetLabel(Label label);

	default void visitLineNumber(int lineNumber)
	{
		Label label = new Label();
		this.visitLabel(label);
		this.visitLineNumber(lineNumber, label);
	}

	@Override
	void visitLineNumber(int line, Label start);

	// Instructions

	@Override
	void visitInsn(int opcode) throws BytecodeException;

	void visitInsnAtLine(int opcode, int lineNumber) throws BytecodeException;

	@Override
	void visitIntInsn(int opcode, int operand) throws BytecodeException;

	@Override
	void visitJumpInsn(int opcode, Label label) throws BytecodeException;

	@Override
	void visitTypeInsn(int opcode, String type) throws BytecodeException;

	@Override
	void visitMultiANewArrayInsn(String type, int dims) throws BytecodeException;

	void visitMultiANewArrayInsn(IType type, int dims) throws BytecodeException;

	@Override
	void visitVarInsn(int opcode, int var) throws BytecodeException;

	@Override
	void visitIincInsn(int index, int value) throws BytecodeException;

	// Field Instructions

	@Override
	default void visitFieldInsn(int opcode, String owner, String name, String desc) throws BytecodeException
	{
		this.visitFieldInsn(opcode, owner, name, desc, Frame.fieldType(desc));
	}

	void visitFieldInsn(int opcode, String owner, String name, String desc, Object fieldType) throws BytecodeException;

	// Invoke Instructions

	@Override
	default void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface)
			throws BytecodeException
	{
		int args = Frame.getArgumentCount(desc);
		if (opcode != Opcodes.INVOKESTATIC)
		{
			args++;
		}
		this.visitMethodInsn(opcode, owner, name, desc, args, Frame.returnType(desc), isInterface);
	}

	void visitMethodInsn(int opcode, String owner, String name, String desc, int args, Object returnType, boolean isInterface)
			throws BytecodeException;

	@Override
	default void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) throws BytecodeException
	{
		this.visitInvokeDynamicInsn(name, desc, Frame.getArgumentCount(desc), Frame.returnType(desc), bsm, bsmArgs);
	}

	void visitInvokeDynamicInsn(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs)
			throws BytecodeException;

	// Switch Instructions

	@Override
	void visitTableSwitchInsn(int start, int end, Label defaultHandler, Label... handlers) throws BytecodeException;

	@Override
	void visitLookupSwitchInsn(Label defaultHandler, int[] keys, Label[] handlers) throws BytecodeException;

	// Blocks

	int startSync();

	void endSync();

	void startCatchBlock(String type);

	void visitFinallyBlock(Label start, Label end, Label handler);

	@Override
	void visitTryCatchBlock(Label start, Label end, Label handler, String type);

	// End

	@Override
	void visitEnd();

	void visitEnd(IType type);
}
