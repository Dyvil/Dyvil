package dyvil.tools.compiler.backend;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface MethodWriter extends AnnotatableVisitor, TypeAnnotatableVisitor
{
	ClassWriter getClassWriter();
	
	Frame getFrame();
	
	void setThisType(String type);
	
	void setLocalType(int index, Object type);
	
	void setHasReturn(boolean hasReturn);
	
	boolean hasReturn();
	
	// Annotations
	
	@Override
	AnnotationVisitor visitAnnotation(String type, boolean visible);
	
	AnnotationVisitor visitParameterAnnotation(int index, String type, boolean visible);
	
	@Override
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	// Code
	
	void begin();
	
	// Parameters
	
	int registerParameter(int index, String name, IType type, int access);
	
	void registerParameter(String name, int access);
	
	// Local Variables
	
	int localCount();
	
	void resetLocals(int count);
	
	void writeLocal(int index, String name, String desc, String signature, Label start, Label end);
	
	// Constants
	
	void writeLDC(int value);
	
	void writeLDC(long value);
	
	void writeLDC(float value);
	
	void writeLDC(double value);
	
	void writeLDC(String value);
	
	void writeLDC(Type value);
	
	// Labels
	
	void writeLabel(Label label);
	
	void writeTargetLabel(Label label);
	
	void writeLineNumber(int lineNumber);
	
	// Instructions
	
	void writeInsn(int opcode) throws BytecodeException;
	
	void writeInsnAtLine(int opcode, int lineNumber) throws BytecodeException;
	
	void writeIntInsn(int opcode, int operand) throws BytecodeException;
	
	void writeJumpInsn(int opcode, Label label) throws BytecodeException;
	
	void writeTypeInsn(int opcode, String type) throws BytecodeException;
	
	void writeNewArray(String type, int dims) throws BytecodeException;
	
	void writeNewArray(IType type, int dims) throws BytecodeException;
	
	void writeVarInsn(int opcode, int var) throws BytecodeException;
	
	void writeIINC(int index, int value) throws BytecodeException;
	
	// Field Instructions
	
	default void writeFieldInsn(int opcode, String owner, String name, String desc) throws BytecodeException
	{
		this.writeFieldInsn(opcode, owner, name, desc, Frame.fieldType(desc));
	}
	
	void writeFieldInsn(int opcode, String owner, String name, String desc, Object fieldType) throws BytecodeException;
	
	// Invoke Instructions
	
	default void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface)
			throws BytecodeException
	{
		int args = Frame.getArgumentCount(desc);
		if (opcode != Opcodes.INVOKESTATIC)
		{
			args++;
		}
		this.writeInvokeInsn(opcode, owner, name, desc, args, Frame.returnType(desc), isInterface);
	}
	
	void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, Object returnType, boolean isInterface)
			throws BytecodeException;
	
	default void writeInvokeDynamic(String name, String desc, Handle bsm, Object... bsmArgs) throws BytecodeException
	{
		this.writeInvokeDynamic(name, desc, Frame.getArgumentCount(desc), Frame.returnType(desc), bsm, bsmArgs);
	}
	
	void writeInvokeDynamic(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs)
			throws BytecodeException;
	
	// Switch Instructions
	
	void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers) throws BytecodeException;
	
	void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers) throws BytecodeException;
	
	// Blocks
	
	int startSync();
	
	void endSync();
	
	void startCatchBlock(String type);
	
	void writeFinallyBlock(Label start, Label end, Label handler);
	
	void writeCatchBlock(Label start, Label end, Label handler, String type);
	
	// End
	
	void end();
	
	void end(IType type);
}
