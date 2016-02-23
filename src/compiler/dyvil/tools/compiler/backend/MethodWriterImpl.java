package dyvil.tools.compiler.backend;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.backend.exception.BytecodeException;

import static dyvil.reflect.Opcodes.*;

public final class MethodWriterImpl implements MethodWriter
{
	private static final Long LONG_MINUS_ONE = -1L;
	
	public    ClassWriter   cw;
	protected MethodVisitor mv;
	
	protected Frame frame = new Frame();
	private boolean visitFrame;
	
	private boolean hasReturn;
	
	private int[] syncLocals;
	private int   syncCount;
	
	public MethodWriterImpl(ClassWriter cw, MethodVisitor mv)
	{
		this.cw = cw;
		this.mv = mv;
	}
	
	@Override
	public Frame getFrame()
	{
		return this.frame;
	}
	
	@Override
	public void setThisType(String type)
	{
		this.frame.setInstance(type);
	}
	
	@Override
	public void setLocalType(int index, Object type)
	{
		this.frame.setLocal(index, type);
	}
	
	@Override
	public void setHasReturn(boolean hasReturn)
	{
		this.hasReturn = hasReturn;
	}
	
	@Override
	public boolean hasReturn()
	{
		return this.hasReturn;
	}
	
	@Override
	public void begin()
	{
		this.mv.visitCode();
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		return this.mv.visitAnnotation(type, visible);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.mv.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public AnnotationVisitor visitParameterAnnotation(int index, String type, boolean visible)
	{
		return this.mv.visitParameterAnnotation(index, type, visible);
	}
	
	// Stack
	
	public void insnCallback()
	{
		this.hasReturn = false;
		if (this.visitFrame)
		{
			this.frame.visitFrame(this.mv);
			this.visitFrame = false;
		}
	}
	
	// Parameters
	
	@Override
	public int registerParameter(int index, String name, IType type, int access)
	{
		this.mv.visitParameter(name, access);
		
		this.frame.setLocal(index, type.getFrameType());
		return this.frame.localCount;
	}
	
	@Override
	public void registerParameter(String name, int access)
	{
		this.mv.visitParameter(name, access);
	}
	
	// Locals
	
	@Override
	public int localCount()
	{
		return this.frame.localCount;
	}
	
	@Override
	public void resetLocals(int count)
	{
		this.frame.localCount = count;
	}
	
	@Override
	public void writeLocal(int index, String name, String desc, String signature, Label start, Label end)
	{
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}
	
	// Constants
	
	@Override
	public void writeLDC(int value)
	{
		this.insnCallback();
		
		this.frame.push(ClassFormat.INT);
		
		switch (value)
		{
		case -1:
			this.mv.visitInsn(ICONST_M1);
			return;
		case 0:
			this.mv.visitInsn(ICONST_0);
			return;
		case 1:
			this.mv.visitInsn(ICONST_1);
			return;
		case 2:
			this.mv.visitInsn(ICONST_2);
			return;
		case 3:
			this.mv.visitInsn(ICONST_3);
			return;
		case 4:
			this.mv.visitInsn(ICONST_4);
			return;
		case 5:
			this.mv.visitInsn(ICONST_5);
			return;
		}
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
		{
			this.mv.visitIntInsn(Opcodes.BIPUSH, value);
			return;
		}
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
		{
			this.mv.visitIntInsn(Opcodes.SIPUSH, value);
			return;
		}
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(long value)
	{
		this.insnCallback();
		
		this.frame.push(ClassFormat.LONG);
		
		if (value == 0L)
		{
			this.mv.visitInsn(LCONST_0);
			return;
		}
		if (value == 1L)
		{
			this.mv.visitInsn(LCONST_1);
			return;
		}
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(float value)
	{
		this.insnCallback();
		
		this.frame.push(ClassFormat.FLOAT);
		
		if (value == 0F)
		{
			this.mv.visitInsn(FCONST_0);
			return;
		}
		if (value == 1F)
		{
			this.mv.visitInsn(FCONST_1);
			return;
		}
		if (value == 2F)
		{
			this.mv.visitInsn(FCONST_2);
			return;
		}
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(double value)
	{
		this.insnCallback();
		
		this.frame.push(ClassFormat.DOUBLE);
		
		if (value == 0D)
		{
			this.mv.visitInsn(DCONST_0);
			return;
		}
		if (value == 1D)
		{
			this.mv.visitInsn(DCONST_1);
			return;
		}
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(String value)
	{
		this.insnCallback();
		
		this.frame.push("java/lang/String");
		
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(Type type)
	{
		this.insnCallback();
		
		this.frame.push("java/lang/Class");
		
		this.mv.visitLdcInsn(type);
	}
	
	// Labels
	
	@Override
	public void writeLabel(Label label)
	{
		if (label.info != null)
		{
			int maxS = this.frame.maxStack;
			int maxL = this.frame.maxLocals;
			this.frame = (Frame) label.info;
			
			if (maxS > this.frame.maxStack)
			{
				this.frame.maxStack = maxS;
			}
			if (maxL > this.frame.maxLocals)
			{
				this.frame.maxLocals = maxL;
			}
			
			this.visitFrame = true;
			label.info = null;
		}
		
		this.mv.visitLabel(label);
	}
	
	@Override
	public void writeTargetLabel(Label label)
	{
		this.writeLabel(label);
		this.visitFrame = true;
		this.hasReturn = false;
	}
	
	@Override
	public void writeLineNumber(int lineNumber)
	{
		Label label = new Label();
		this.mv.visitLabel(label);
		this.mv.visitLineNumber(lineNumber, label);
	}
	
	// Other Instructions
	
	@Override
	public void writeInsnAtLine(int opcode, int lineNumber) throws BytecodeException
	{
		switch (opcode)
		{
		// NullPointerException, ArrayIndexOutOfBoundsException
		case ARRAYLENGTH:
		case IALOAD:
		case LALOAD:
		case FALOAD:
		case DALOAD:
		case AALOAD:
		case BALOAD:
		case CALOAD:
		case SALOAD:
		case IASTORE:
		case LASTORE:
		case FASTORE:
		case DASTORE:
		case BASTORE:
		case CASTORE:
		case SASTORE:
			// ..., ArrayStoreException
		case AASTORE:
			// NullPointerException, any unchecked Exception
		case OBJECT_EQUALS:
			// ArithmeticException
		case IDIV:
		case LDIV:
			this.writeLineNumber(lineNumber);
		}
		
		this.writeInsn(opcode);
	}
	
	@Override
	public void writeInsn(int opcode) throws BytecodeException
	{
		if (opcode > 255)
		{
			switch (opcode)
			{
			case Opcodes.LCONST_M1:
				this.frame.push(ClassFormat.LONG);
				this.mv.visitLdcInsn(LONG_MINUS_ONE);
				return;
			case Opcodes.BNOT:
				this.writeBoolJump(Opcodes.IFEQ);
				return;
			case Opcodes.INOT:
				this.frame.reserve(1);
				this.mv.visitInsn(Opcodes.ICONST_M1);
				this.mv.visitInsn(Opcodes.IXOR);
				return;
			case Opcodes.LNOT:
				this.frame.reserve(2);
				this.mv.visitLdcInsn(LONG_MINUS_ONE);
				this.mv.visitInsn(Opcodes.LXOR);
				return;
			case Opcodes.L2B:
				this.frame.set(ClassFormat.BYTE);
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.L2S:
				this.frame.set(ClassFormat.SHORT);
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.L2C:
				this.frame.set(ClassFormat.CHAR);
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case Opcodes.F2B:
				this.frame.set(ClassFormat.BYTE);
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.F2S:
				this.frame.set(ClassFormat.SHORT);
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.F2C:
				this.frame.set(ClassFormat.CHAR);
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case Opcodes.D2B:
				this.frame.set(ClassFormat.BYTE);
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.D2S:
				this.frame.set(ClassFormat.SHORT);
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.D2C:
				this.frame.set(ClassFormat.CHAR);
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case NULL:
				this.writeBoolJump(IFNULL);
				return;
			case NONNULL:
				this.writeBoolJump(IFNONNULL);
				return;
			case ACMPEQ:
				this.writeBoolJump(Opcodes.IF_ACMPEQ);
				return;
			case ACMPNE:
				this.writeBoolJump(Opcodes.IF_ACMPNE);
				return;
			case Opcodes.OBJECT_EQUALS:
				this.frame.pop();
				this.frame.pop();
				this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z",
				                        false);
				this.frame.push(ClassFormat.BOOLEAN);
				return;
			case SWAP2:
				this.frame.reserve(2);
				this.mv.visitInsn(Opcodes.DUP2_X2);
				this.mv.visitInsn(Opcodes.POP2);
				return;
			case Opcodes.AUTO_SWAP:
				BackendUtil.swap(this);
				return;
			case Opcodes.AUTO_POP:
				BackendUtil.pop(this);
				return;
			case Opcodes.AUTO_POP2:
				BackendUtil.pop2(this);
				return;
			case Opcodes.AUTO_DUP:
				BackendUtil.dup(this);
				return;
			case Opcodes.AUTO_DUP_X1:
				BackendUtil.dupX1(this);
				return;
			}

			if (opcode >= EQ0 && opcode <= LE0)
			{
				this.writeBoolJump(IFEQ + opcode - EQ0);
			}
			if (opcode >= ICMPEQ && opcode <= ICMPLE)
			{
				this.writeBoolJump(IF_ICMPEQ + opcode - ICMPEQ);
				return;
			}
			if (opcode >= LCMPEQ && opcode <= DCMPLE)
			{
				this.writeBoolJump(IF_LCMPEQ + opcode - LCMPEQ);
				return;
			}
			return;
		}
		
		this.insnCallback();
		
		this.frame.visitInsn(opcode);
		
		if (opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW)
		{
			if (this.syncCount > 0)
			{
				for (int i = 0; i < this.syncCount; i++)
				{
					this.mv.visitVarInsn(Opcodes.ALOAD, this.syncLocals[i]);
					this.mv.visitInsn(Opcodes.MONITOREXIT);
				}
			}
			this.visitFrame = true;
			this.hasReturn = true;
		}
		this.mv.visitInsn(opcode);
	}
	
	private void writeBoolJump(int jump) throws BytecodeException
	{
		Label label1 = new Label();
		Label label2 = new Label();
		
		this.writeJumpInsn(jump, label1);
		
		this.writeInsn(Opcodes.ICONST_0);
		this.writeJumpInsn(Opcodes.GOTO, label2);
		this.writeTargetLabel(label1);
		this.writeInsn(Opcodes.ICONST_1);
		this.writeTargetLabel(label2);
	}
	
	@Override
	public void writeIntInsn(int opcode, int operand) throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitIntInsn(opcode, operand);
		
		this.mv.visitIntInsn(opcode, operand);
	}
	
	// Jump Instructions
	
	@Override
	public void writeJumpInsn(int opcode, Label target) throws BytecodeException
	{
		if (opcode > 255)
		{
			switch (opcode)
			{
			case Opcodes.IF_LCMPEQ:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_LCMPNE:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_LCMPLT:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_LCMPGE:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_LCMPGT:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_LCMPLE:
				this.writeInsn(Opcodes.LCMP);
				this.writeJumpInsn(Opcodes.IFLE, target);
				return;
			case Opcodes.IF_FCMPEQ:
				this.writeInsn(Opcodes.FCMPL);
				this.writeJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_FCMPNE:
				this.writeInsn(Opcodes.FCMPL);
				this.writeJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_FCMPLT:
				this.writeInsn(Opcodes.FCMPL);
				this.writeJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_FCMPGE:
				this.writeInsn(Opcodes.FCMPG);
				this.writeJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_FCMPGT:
				this.writeInsn(Opcodes.FCMPG);
				this.writeJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_FCMPLE:
				this.writeInsn(Opcodes.FCMPL);
				this.writeJumpInsn(Opcodes.IFLE, target);
				return;
			case Opcodes.IF_DCMPEQ:
				this.writeInsn(Opcodes.DCMPL);
				this.writeJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_DCMPNE:
				this.writeInsn(Opcodes.DCMPL);
				this.writeJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_DCMPLT:
				this.writeInsn(Opcodes.DCMPL);
				this.writeJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_DCMPGE:
				this.writeInsn(Opcodes.DCMPG);
				this.writeJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_DCMPGT:
				this.writeInsn(Opcodes.DCMPG);
				this.writeJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_DCMPLE:
				this.writeInsn(Opcodes.DCMPL);
				this.writeJumpInsn(Opcodes.IFLE, target);
				return;
			}
		}
		
		this.insnCallback();
		
		this.visitFrame = true;
		this.frame.visitJumpInsn(opcode);
		
		if (target.info == null)
		{
			target.info = this.frame.copy();
		}
		
		this.mv.visitJumpInsn(opcode, target);
	}
	
	@Override
	public void writeTypeInsn(int opcode, String type) throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitTypeInsn(opcode, type);
		
		this.mv.visitTypeInsn(opcode, type);
	}
	
	@Override
	public void writeNewArray(String type, int dims) throws BytecodeException
	{
		if (dims == 1)
		{
			this.writeTypeInsn(Opcodes.ANEWARRAY, type);
			return;
		}
		
		this.insnCallback();
		
		this.frame.visitNewArray(type, dims);
		
		this.mv.visitMultiANewArrayInsn(type, dims);
	}
	
	private static int getNewArrayCode(int typecode)
	{
		switch (typecode)
		{
		case PrimitiveType.BOOLEAN_CODE:
			return ClassFormat.T_BOOLEAN;
		case PrimitiveType.BYTE_CODE:
			return ClassFormat.T_BYTE;
		case PrimitiveType.SHORT_CODE:
			return ClassFormat.T_SHORT;
		case PrimitiveType.CHAR_CODE:
			return ClassFormat.T_CHAR;
		case PrimitiveType.INT_CODE:
			return ClassFormat.T_INT;
		case PrimitiveType.LONG_CODE:
			return ClassFormat.T_LONG;
		case PrimitiveType.FLOAT_CODE:
			return ClassFormat.T_FLOAT;
		case PrimitiveType.DOUBLE_CODE:
			return ClassFormat.T_DOUBLE;
		}
		return 0;
	}
	
	@Override
	public void writeNewArray(IType type, int dims) throws BytecodeException
	{
		if (dims == 1)
		{
			if (type.isPrimitive())
			{
				this.writeIntInsn(Opcodes.NEWARRAY, getNewArrayCode(type.getTypecode()));
				return;
			}
			
			this.writeTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
			return;
		}
		
		this.insnCallback();
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < dims; i++)
		{
			builder.append('[');
		}
		type.appendExtendedName(builder);
		String extended = builder.toString();
		this.frame.visitNewArray(extended, dims);
		
		this.mv.visitMultiANewArrayInsn(extended, dims);
	}
	
	@Override
	public void writeIINC(int index, int value) throws BytecodeException
	{
		this.insnCallback();
		
		this.mv.visitIincInsn(index, value);
	}
	
	@Override
	public void writeVarInsn(int opcode, int index) throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitVarInsn(opcode, index);
		
		this.mv.visitVarInsn(opcode, index);
	}
	
	@Override
	public void writeFieldInsn(int opcode, String owner, String name, String desc, Object fieldType)
			throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitFieldInsn(opcode, fieldType);
		
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, Object returnType, boolean isInterface)
			throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitInvokeInsn(args, returnType);
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}
	
	@Override
	public void writeInvokeDynamic(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs)
			throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitInvokeInsn(args, returnType);
		
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	
	// Switch Instructions
	
	@Override
	public void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers) throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitInsn(Opcodes.TABLESWITCH);
		
		defaultHandler.info = this.frame.copy();
		for (Label l : handlers)
		{
			l.info = this.frame.copy();
		}
		
		this.mv.visitTableSwitchInsn(start, end, defaultHandler, handlers);
	}
	
	@Override
	public void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers) throws BytecodeException
	{
		this.insnCallback();
		
		this.frame.visitInsn(Opcodes.LOOKUPSWITCH);
		
		defaultHandler.info = this.frame.copy();
		for (Label l : handlers)
		{
			l.info = this.frame.copy();
		}
		
		this.mv.visitLookupSwitchInsn(defaultHandler, keys, handlers);
	}
	
	// Inlining
	
	@Override
	public int startSync()
	{
		if (this.syncLocals == null)
		{
			this.syncLocals = new int[1];
			this.syncCount = 1;
			return this.syncLocals[0] = this.frame.localCount;
		}
		
		int index = this.syncCount++;
		if (index >= this.syncLocals.length)
		{
			int[] temp = new int[this.syncCount];
			System.arraycopy(this.syncLocals, 0, temp, 0, this.syncLocals.length);
			this.syncLocals = temp;
		}
		return this.syncLocals[index] = this.frame.localCount;
	}
	
	@Override
	public void endSync()
	{
		this.syncCount--;
	}
	
	@Override
	public void startCatchBlock(String type)
	{
		this.frame.push(type);
	}
	
	@Override
	public void writeFinallyBlock(Label start, Label end, Label handler)
	{
		this.mv.visitTryCatchBlock(start, end, handler, null);
	}
	
	@Override
	public void writeCatchBlock(Label start, Label end, Label handler, String type)
	{
		this.mv.visitTryCatchBlock(start, end, handler, type);
	}
	
	@Override
	public void end()
	{
		this.mv.visitMaxs(this.frame.maxStack, this.frame.maxLocals);
		this.mv.visitEnd();
	}
	
	@Override
	public void end(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			iclass.writeInnerClassInfo(this.cw);
		}
		
		if (!this.hasReturn)
		{
			int opcode = type.getReturnOpcode();
			if (opcode == RETURN || this.frame.actualStackCount > 0)
			{
				this.insnCallback();
				this.mv.visitInsn(opcode);
			}
		}
		this.mv.visitMaxs(this.frame.maxStack, this.frame.maxLocals);
		this.mv.visitEnd();
	}
	
	@Override
	public String toString()
	{
		return "MethodWriter(frame: " + this.frame + ")";
	}
}
