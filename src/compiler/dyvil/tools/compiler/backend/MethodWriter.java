package dyvil.tools.compiler.backend;

import org.objectweb.asm.*;

import dyvil.tools.compiler.ast.type.IType;

public interface MethodWriter
{
	int		H_GETFIELD			= Opcodes.H_GETFIELD;
	int		H_GETSTATIC			= Opcodes.H_GETSTATIC;
	int		H_PUTFIELD			= Opcodes.H_PUTFIELD;
	int		H_PUTSTATIC			= Opcodes.H_PUTSTATIC;
	int		H_INVOKEVIRTUAL		= Opcodes.H_INVOKEVIRTUAL;
	int		H_INVOKESTATIC		= Opcodes.H_INVOKESTATIC;
	int		H_INVOKESPECIAL		= Opcodes.H_INVOKESPECIAL;
	int		H_NEWINVOKESPECIAL	= Opcodes.H_NEWINVOKESPECIAL;
	int		H_INVOKEINTERFACE	= Opcodes.H_INVOKEINTERFACE;
	
	int		T_BOOLEAN			= 4;
	int		T_CHAR				= 5;
	int		T_FLOAT				= 6;
	int		T_DOUBLE			= 7;
	int		T_BYTE				= 8;
	int		T_SHORT				= 9;
	int		T_INT				= 10;
	int		T_LONG				= 11;
	
	Integer	UNINITIALIZED_THIS	= Opcodes.UNINITIALIZED_THIS;
	Integer	NULL				= Opcodes.NULL;
	Integer	TOP					= Opcodes.TOP;
	Integer	INT					= Opcodes.INTEGER;
	Integer	LONG				= Opcodes.LONG;
	Integer	FLOAT				= Opcodes.FLOAT;
	Integer	DOUBLE				= Opcodes.DOUBLE;
	
	public void setConstructor(IType type);
	
	public void setInstance(IType type);
	
	// Annotations
	
	public AnnotationVisitor addAnnotation(String type, boolean visible);
	
	public AnnotationVisitor addParameterAnnotation(int index, String type, boolean visible);
	
	// Code
	
	public void begin();
	
	// Stack
	
	public int stackCount();
	
	public void set(Object type);
	
	public void push(Object type);
	
	public void pop();
	
	public void pop(int count);
	
	public void clear();
	
	public Object peek();
	
	public void writeFrame();
	
	public void writeFrame(int type, int stackCount, Object[] stack, int localCount, Object[] locals);
	
	// Parameters
	
	public int registerParameter(String name, Object type);
	
	public int registerParameter(String name, IType type);
	
	// Local Variables
	
	public int localCount();
	
	public void resetLocals(int count);
	
	public Object getLocal(int index);
	
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
	
	public void writeFrameLabel(Label label);
	
	// Instructions
	
	public void writeInsn(int opcode);
	
	public void writeJumpInsn(int opcode, Label label);
	
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
	
	// Inlining
	
	public void startInline(int varOffset, int stackOffset, Label end);
	
	public void endInline(int varOffset, int stackOffset, Label end);
	
	// Blocks
	
	public void writeFinallyBlock(Label start, Label end, Label handler);
	
	public void writeTryCatchBlock(Label start, Label end, Label handler, String type);
	
	public void writeTryCatchBlock(Label start, Label end, Label handler, IType type);
	
	// End
	
	public void end();
	
	public void end(IType type);
}
