package dyvil.tools.compiler.backend;

import org.objectweb.asm.*;

import dyvil.tools.compiler.ast.type.IType;

public interface MethodWriter
{
	public void setInstanceMethod();
	
	// Annotations
	
	public AnnotationVisitor addAnnotation(String type, boolean visible);
	
	public AnnotationVisitor addParameterAnnotation(int index, String type, boolean visible);
	
	// Code
	
	public void begin();
	
	// Parameters
	
	public int registerParameter(String name, Object type);
	
	// Local Variables
	
	public int localCount();
	
	public void resetLocals(int count);
	
	public void writeLocal(String name, String desc, String signature, Label start, Label end, int index);
	
	public void writeLocal(String name, IType type, Label start, Label end, int index);
	
	// Constants
	
	public void writeLDC(int value);
	
	public void writeLDC(long value);
	
	public void writeLDC(float value);
	
	public void writeLDC(double value);
	
	public void writeLDC(String value);
	
	public void writeLDC(Type value);
	
	// Labels
	
	public void writeLabel(Label label);
	
	// Instructions
	
	public void writeInsn(int opcode);
	
	public void writeIntInsn(int opcode, int operand);
	
	public void writeJumpInsn(int opcode, Label label);
	
	public void writeTypeInsn(int opcode, String type);
	
	public void writeNewArray(String type, int dims);
	
	public void writeNewArray(IType type, int dims);
	
	public void writeVarInsn(int opcode, int var);
	
	public void writeIINC(int var, int value);
	
	// Field Instructions
	
	public void writeFieldInsn(int opcode, String owner, String name, String desc);
	
	// Invoke Instructions
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface);
	
	public void writeInvokeDynamic(String name, String desc, Handle bsm, Object... bsmArgs);
	
	// Switch Instructions
	
	public void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers);
	
	public void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers);
	
	// Inlining
	
	public void startInline(Label end);
	
	public void endInline(Label end);
	
	// Blocks
	
	public void writeFinallyBlock(Label start, Label end, Label handler);
	
	public void writeTryCatchBlock(Label start, Label end, Label handler, String type);
	
	// End
	
	public void end();
	
	public void end(IType type);
}
