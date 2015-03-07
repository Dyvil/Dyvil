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
	
	public AnnotationVisitor addAnnotation(String type, boolean visible);
	
	public AnnotationVisitor addParameterAnnotation(int index, String type, boolean visible);
	
	// Code
	
	public void begin();
	
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
	
	public int registerLocal(Object type);
	
	public int registerLocal(IType type);
	
	public void removeLocals(int count);
	
	public void writeLocal(String name, String desc, String signature, Label start, Label end, int index);
	
	public void writeLocal(String name, IType type, Label start, Label end, int index);
	
	// Constants
	
	public void writeLDC(int value);
	
	public void writeLDC(long value);
	
	public void writeLDC(float value);
	
	public void writeLDC(double value);
	
	public void writeLDC(String value);
	
	public void writeLDC(Object value);
	
	// Labels
	
	public void writeFrameLabel(Label label);
	
	public void writeLabel(Label label);
	
	// Instructions
	
	public void writeInsn(int opcode);
	
	public void writeFrameJump(int opcode, Label label);
	
	public void writeJump(int opcode, Label label);
	
	public void writeTypeInsn(int opcode, String type);
	
	public void writeTypeInsn(int opcode, IType type);
	
	public void writeNewArray(String type, int dims);
	
	public void writeNewArray(IType type, int dims);
	
	public void writeVarInsn(int opcode, int var);
	
	public void writeIINC(int var, int value);
	
	// Field Instructions
	
	public void writeGetStatic(String owner, String name, String desc, IType type);
	
	public void writePutStatic(String owner, String name, String desc);
	
	public void writeGetField(String owner, String name, String desc, IType type);
	
	public void writePutField(String owner, String name, String desc);
	
	// Invoke Instructions
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, Object returnType);
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, IType returnType);
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, Object returnType);
	
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, IType returnType);
	
	public void writeInvokeDynamic(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs);
	
	public void writeInvokeDynamic(String name, String desc, int args, IType returnType, Handle bsm, Object... bsmArgs);
	
	// End
	
	public void end();
	
	public void end(IType type);
}
