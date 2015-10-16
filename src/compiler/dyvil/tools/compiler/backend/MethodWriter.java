package dyvil.tools.compiler.backend;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface MethodWriter extends AnnotatableVisitor, TypeAnnotatableVisitor
{
	public ClassWriter getClassWriter();
	
	public Frame getFrame();
	
	public void setThisType(String type);
	
	public void setLocalType(int index, Object type);
	
	public void setHasReturn(boolean hasReturn);
	
	public boolean hasReturn();
	
	// Annotations
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible);
	
	public AnnotationVisitor visitParameterAnnotation(int index, String type, boolean visible);
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	// Code
	
	public void begin();
	
	// Parameters
	
	public int registerParameter(int index, String name, IType type, int access);
	
	public void registerParameter(String name, int access);
	
	// Local Variables
	
	public int localCount();
	
	public void resetLocals(int count);
	
	public void writeLocal(int index, String name, String desc, String signature, Label start, Label end);
	
	// Constants
	
	public void writeLDC(int value);
	
	public void writeLDC(long value);
	
	public void writeLDC(float value);
	
	public void writeLDC(double value);
	
	public void writeLDC(String value);
	
	public void writeLDC(Type value);
	
	// Labels
	
	public void writeLabel(Label label);
	
	public void writeTargetLabel(Label label);
	
	public void writeLineNumber(int lineNumber);
	
	// Instructions
	
	public void writeInsn(int opcode) throws BytecodeException;
	
	public void writeInsn(int opcode, int lineNumber) throws BytecodeException;
	
	public void writeIntInsn(int opcode, int operand) throws BytecodeException;
	
	public void writeJumpInsn(int opcode, Label label) throws BytecodeException;
	
	public void writeTypeInsn(int opcode, String type) throws BytecodeException;
	
	public void writeNewArray(String type, int dims) throws BytecodeException;
	
	public void writeNewArray(IType type, int dims) throws BytecodeException;
	
	public void writeVarInsn(int opcode, int var) throws BytecodeException;
	
	public void writeIINC(int index, int value) throws BytecodeException;
	
	// Field Instructions
	
	public default void writeFieldInsn(int opcode, String owner, String name, String desc) throws BytecodeException
	{
		this.writeFieldInsn(opcode, owner, name, desc, Frame.fieldType(desc));
	}
	
	public void writeFieldInsn(int opcode, String owner, String name, String desc, Object fieldType) throws BytecodeException;
	
	// Invoke Instructions
	
	public default void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface) throws BytecodeException
	{
		int args = Frame.getArgumentCount(desc);
		if (opcode != Opcodes.INVOKESTATIC)
		{
			args++;
		}
		this.writeInvokeInsn(opcode, owner, name, desc, args, Frame.returnType(desc), isInterface);
	}
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, Object returnType, boolean isInterface) throws BytecodeException;
	
	public default void writeInvokeDynamic(String name, String desc, Handle bsm, Object... bsmArgs) throws BytecodeException
	{
		this.writeInvokeDynamic(name, desc, Frame.getArgumentCount(desc), Frame.returnType(desc), bsm, bsmArgs);
	}
	
	public void writeInvokeDynamic(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs) throws BytecodeException;
	
	// Switch Instructions
	
	public void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers) throws BytecodeException;
	
	public void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers) throws BytecodeException;
	
	// Blocks
	
	public int startSync();
	
	public void endSync();
	
	public void startCatchBlock(String type);
	
	public void writeFinallyBlock(Label start, Label end, Label handler);
	
	public void writeCatchBlock(Label start, Label end, Label handler, String type);
	
	// End
	
	public void end();
	
	public void end(IType type);
}
