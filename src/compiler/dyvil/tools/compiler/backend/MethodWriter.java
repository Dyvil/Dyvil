package dyvil.tools.compiler.backend;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;

public final class MethodWriter extends MethodVisitor
{
	public static final Long	LONG_MINUS_ONE	= Long.valueOf(-1);
	public static final Integer	TOP				= jdk.internal.org.objectweb.asm.Opcodes.TOP;
	public static final Integer	INT				= jdk.internal.org.objectweb.asm.Opcodes.INTEGER;
	public static final Integer	LONG			= jdk.internal.org.objectweb.asm.Opcodes.LONG;
	public static final Integer	FLOAT			= jdk.internal.org.objectweb.asm.Opcodes.FLOAT;
	public static final Integer	DOUBLE			= jdk.internal.org.objectweb.asm.Opcodes.DOUBLE;
	
	public ClassWriter			cw;
	
	public boolean				hasReturn;
	
	private int					localIndex;
	private int					localCount;
	private int					maxLocals;
	private Object[]			locals			= new Object[2];
	
	private int					stackIndex;
	private int					stackCount;
	private int					maxStack;
	private Object[]			stack			= new Object[3];
	
	public MethodWriter(ClassWriter cw, MethodVisitor mv)
	{
		super(ASM5, mv);
		this.cw = cw;
	}
	
	public void setConstructor(IType type)
	{
		this.locals[0] = UNINITIALIZED_THIS;
		this.push(UNINITIALIZED_THIS);
	}
	
	// Locals
	
	private void ensureLocals(int count)
	{
		if (count > this.locals.length)
		{
			Object[] newLocals = new Object[count];
			System.arraycopy(this.locals, 0, newLocals, 0, this.locals.length);
			this.locals = newLocals;
		}
		if (count > this.maxLocals)
		{
			this.maxLocals = count;
			this.localCount = count;
			return;
		}
		if (count > this.localCount)
		{
			this.localCount = count;
		}
	}
	
	@Deprecated
	public void addLocal(int index, IType type)
	{
		this.ensureLocals(index + 1);
		this.locals[index] = type.getFrameType();
		this.localIndex = index + 1;
	}
	
	@Deprecated
	public void addLocal(int index, Object type)
	{
		this.ensureLocals(index + 1);
		this.locals[index] = type;
		this.localIndex = index + 1;
	}
	
	public int addLocal(IType type)
	{
		return this.addLocal(type.getFrameType());
	}
	
	public int addLocal(Object type)
	{
		int index = this.localIndex;
		if (type == LONG || type == DOUBLE)
		{
			this.ensureLocals(index + 2);
			this.locals[index] = type;
			this.locals[index + 1] = type;
			this.localIndex += 2;
		}
		else
		{
			this.ensureLocals(index + 1);
			this.locals[index] = type;
			this.localIndex++;
		}
		
		return index;
	}
	
	public void removeLocals(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.localCount--;
			Object o = this.locals[--this.localIndex];
			if (o == LONG || o == DOUBLE)
			{
				this.localIndex--;
				this.localCount--;
			}
		}
	}
	
	// Stack Management
	
	private void ensureStack(int count)
	{
		if (count > this.stack.length)
		{
			Object[] newLocals = new Object[count];
			System.arraycopy(this.stack, 0, newLocals, 0, this.stack.length);
			this.stack = newLocals;
			this.stackCount = count;
			this.maxStack = count;
			return;
		}
		if (count > this.maxStack)
		{
			this.maxStack = count;
			this.stackCount = count;
			return;
		}
		if (count > this.stackCount)
		{
			this.stackCount = count;
		}
	}
	
	protected void set(Object type)
	{
		this.stack[this.stackIndex - 1] = type;
	}
	
	protected void push(Object type)
	{
		if (type == LONG || type == DOUBLE)
		{
			if (this.stackIndex + 3 > this.maxStack)
			{
				this.maxStack = this.stackIndex + 3;
			}
		}
		this.ensureStack(this.stackIndex + 1);
		this.stack[this.stackIndex++] = type;
	}
	
	public void push(IType type)
	{
		Object frameType = type.getFrameType();
		if (frameType != null)
		{
			this.push(frameType);
		}
	}
	
	public void pop()
	{
		this.stackIndex--;
		this.stackCount--;
	}
	
	public Object peek()
	{
		return this.stack[this.stackIndex];
	}
	
	private void visitFrame()
	{
		this.mv.visitFrame(F_NEW, this.localCount, this.locals, this.stackCount, this.stack);
	}
	
	// Parameters
	
	@Override
	@Deprecated
	public void visitParameter(String desc, int index)
	{
		this.mv.visitParameter(desc, index);
	}
	
	public int visitParameter(String name, IType type)
	{
		int index = this.addLocal(type.getFrameType());
		this.mv.visitParameter(name, index);
		
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			iclass.writeInnerClassInfo(this.cw);
		}
		return index;
	}
	
	public int visitParameter(String name, Object type)
	{
		int index = this.addLocal(type);
		this.mv.visitParameter(name, index);
		return index;
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}
	
	public void visitLocalVariable(String name, IType type, Label start, Label end, int index)
	{
		this.mv.visitLocalVariable(name, type.getExtendedName(), type.getSignature(), start, end, index);
	}
	
	// Constant Loading
	
	public void visitLdcInsn(int value)
	{
		this.push(INTEGER);
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
		if (value > 0)
		{
			if (value <= Byte.MAX_VALUE)
			{
				this.mv.visitIntInsn(Opcodes.BIPUSH, value);
				return;
			}
			if (value <= Short.MAX_VALUE)
			{
				this.mv.visitIntInsn(Opcodes.SIPUSH, value);
				return;
			}
		}
		this.mv.visitLdcInsn(Integer.valueOf(value));
	}
	
	public void visitLdcInsn(long value)
	{
		this.push(LONG);
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
		this.mv.visitLdcInsn(Long.valueOf(value));
	}
	
	public void visitLdcInsn(float value)
	{
		this.push(FLOAT);
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
		this.mv.visitLdcInsn(Float.valueOf(value));
	}
	
	public void visitLdcInsn(double value)
	{
		this.push(DOUBLE);
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
		this.mv.visitLdcInsn(Double.valueOf(value));
	}
	
	public void visitLdcInsn(String value)
	{
		this.push("Ljava/lang/String;");
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	@Deprecated
	public void visitLdcInsn(Object obj)
	{
		Class c = obj.getClass();
		if (c == String.class)
		{
			this.push("Ljava/lang/String;");
		}
		else if (c == Integer.class)
		{
			this.push(INTEGER);
		}
		else if (c == Long.class)
		{
			this.push(LONG);
		}
		else if (c == Float.class)
		{
			this.push(FLOAT);
		}
		else if (c == Double.class)
		{
			this.push(DOUBLE);
		}
		this.mv.visitLdcInsn(obj);
	}
	
	// Labels
	
	@Override
	public void visitLabel(Label label)
	{
		this.visitFrame();
		this.mv.visitLabel(label);
	}
	
	public void visitLabel(Label label, boolean stack)
	{
		if (stack)
		{
			this.visitFrame();
		}
		this.mv.visitLabel(label);
	}
	
	// Other Instructions
	
	@Override
	public void visitInsn(int opcode)
	{
		if (opcode > 255)
		{
			this.visitSpecialInsn(opcode);
			return;
		}
		else if (opcode >= IASTORE && opcode <= SASTORE)
		{
			this.stackIndex -= 3;
			this.stackCount -= 3;
		}
		else if (opcode >= IRETURN && opcode <= ARETURN)
		{
			this.pop();
		}
		else
		{
			this.processInsn(opcode);
		}
		this.mv.visitInsn(opcode);
	}
	
	private void processInsn(int opcode)
	{
		switch (opcode)
		{
		case DUP:
		{
			this.ensureStack(this.stackIndex + 1);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 1];
			this.stackIndex++;
			return;
		}
		case DUP_X1:
		{
			this.ensureStack(this.stackIndex + 1);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 2];
			this.stackIndex++;
			return;
		}
		case DUP_X2:
		{
			this.ensureStack(this.stackIndex + 1);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 3];
			this.stackIndex++;
			return;
		}
		case DUP2:
		{
			this.ensureStack(this.stackIndex + 2);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 2];
			this.stack[this.stackIndex + 1] = this.stack[this.stackIndex - 1];
			this.stackIndex += 2;
			return;
		}
		case DUP2_X1:
		{
			this.ensureStack(this.stackIndex + 2);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 3];
			this.stack[this.stackIndex + 1] = this.stack[this.stackIndex - 2];
			this.stackIndex += 2;
			return;
		}
		case DUP2_X2:
		{
			this.ensureStack(this.stackIndex + 2);
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 4];
			this.stack[this.stackIndex + 1] = this.stack[this.stackIndex - 3];
			this.stackIndex += 2;
			return;
		}
		case SWAP:
		{
			Object o = this.stack[this.stackIndex];
			this.stack[this.stackIndex] = this.stack[this.stackIndex - 1];
			this.stack[this.stackIndex - 1] = o;
			return;
		}
		case POP:
		{
			this.stackIndex--;
			this.stackCount--;
			return;
		}
		case POP2:
		{
			this.stackIndex -= 2;
			this.stackCount -= 2;
			return;
		}
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case ICONST_M1:
			this.push(INTEGER);
			return;
		case LCONST_0:
		case LCONST_1:
			this.push(LONG);
			return;
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			this.push(FLOAT);
			return;
		case DCONST_0:
		case DCONST_1:
			this.push(DOUBLE);
			return;
		case ACONST_NULL:
			this.push(NULL);
			return;
		case ARRAYLENGTH:
			this.set(INTEGER);
			return;
		case RETURN:
			this.hasReturn = true;
			return;
		case BALOAD:
		case SALOAD:
		case CALOAD:
		case IALOAD:
			this.pop();
			this.pop();
			this.push(INTEGER);
			return;
		case LALOAD:
			this.pop();
			this.pop();
			this.push(LONG);
			return;
		case FALOAD:
			this.pop();
			this.pop();
			this.push(FLOAT);
			return;
		case DALOAD:
			this.pop();
			this.pop();
			this.push(DOUBLE);
			return;
		case AALOAD:
			this.pop();
			this.pop();
			this.push(TOP);
			return;
		case IADD:
		case ISUB:
		case IMUL:
		case IDIV:
		case IREM:
		case ISHL:
		case ISHR:
		case IUSHR:
			this.pop();
			this.pop();
			this.push(INTEGER);
			return;
		case LADD:
		case LSUB:
		case LMUL:
		case LDIV:
		case LREM:
		case LSHL:
		case LSHR:
		case LUSHR:
			this.pop();
			this.pop();
			this.push(LONG);
			return;
		case FADD:
		case FSUB:
		case FMUL:
		case FDIV:
		case FREM:
			this.pop();
			this.pop();
			this.push(FLOAT);
			return;
		case DADD:
		case DSUB:
		case DMUL:
		case DDIV:
		case DREM:
			this.pop();
			this.pop();
			this.push(DOUBLE);
			return;
			// Casts
		case L2I:
		case F2I:
		case D2I:
			this.set(INTEGER);
			return;
		case I2L:
		case F2L:
		case D2L:
			this.set(LONG);
			return;
		case I2F:
		case L2F:
		case D2F:
			this.set(FLOAT);
			return;
			// Comparison Operators
		case LCMP:
		case FCMPL:
		case FCMPG:
		case DCMPL:
		case DCMPG:
			this.pop();
			this.pop();
			this.push(INTEGER);
			return;
		}
	}
	
	public void visitSpecialInsn(int opcode)
	{
		switch (opcode)
		{
		case Opcodes.LCONST_M1:
			this.mv.visitLdcInsn(LONG_MINUS_ONE);
			return;
		case Opcodes.IBIN:
			this.mv.visitInsn(Opcodes.ICONST_M1);
			this.mv.visitInsn(Opcodes.IXOR);
			return;
		case Opcodes.LBIN:
			this.mv.visitLdcInsn(LONG_MINUS_ONE);
			this.mv.visitInsn(Opcodes.IXOR);
			return;
		case Opcodes.L2B:
			this.mv.visitInsn(Opcodes.L2I);
			this.mv.visitInsn(Opcodes.I2B);
			this.set(INTEGER);
			return;
		case Opcodes.L2S:
			this.mv.visitInsn(Opcodes.L2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INTEGER);
			return;
		case Opcodes.L2C:
			this.mv.visitInsn(Opcodes.L2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INTEGER);
			return;
		case Opcodes.F2B:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2B);
			this.set(INTEGER);
			return;
		case Opcodes.F2S:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INTEGER);
			return;
		case Opcodes.F2C:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INTEGER);
			return;
		case Opcodes.D2B:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2B);
			this.set(INTEGER);
			return;
		case Opcodes.D2S:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INTEGER);
			return;
		case Opcodes.D2C:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INTEGER);
			return;
		}
	}
	
	// Jump Instructions
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
		if (opcode > 255)
		{
			visitSpecialJumpInsn(opcode, label);
			return;
		}
		if (opcode >= IFEQ && opcode <= IFLE)
		{
			this.visitFrame();
			this.pop();
		}
		if (opcode == IFNULL || opcode == IFNONNULL)
		{
			this.visitFrame();
			this.pop();
		}
		if (opcode == IF_ACMPEQ || opcode == IF_ACMPNE)
		{
			this.visitFrame();
			this.stackIndex -= 2;
			this.stackCount -= 2;
		}
		if (opcode >= IF_ICMPEQ && opcode <= IF_ICMPLE)
		{
			this.visitFrame();
			this.stackIndex -= 2;
			this.stackCount -= 2;
		}
		this.mv.visitJumpInsn(opcode, label);
	}
	
	public void visitJumpInsn2(int opcode, Label label)
	{
		if (opcode >= IFEQ && opcode <= IFLE)
		{
			this.pop();
		}
		if (opcode >= IF_ICMPEQ && opcode <= IF_ICMPLE)
		{
			this.stackIndex -= 2;
			this.stackCount -= 2;
		}
		this.mv.visitJumpInsn(opcode, label);
	}
	
	private void visitSpecialJumpInsn(int opcode, Label dest)
	{
		switch (opcode)
		{
		case Opcodes.IF_LCMPEQ:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_LCMPNE:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_LCMPLT:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_LCMPGE:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_LCMPGT:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_LCMPLE:
			this.visitInsn(Opcodes.LCMP);
			this.visitJumpInsn(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_FCMPEQ:
			this.visitInsn(Opcodes.FCMPL);
			this.visitJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_FCMPNE:
			this.visitInsn(Opcodes.FCMPL);
			this.visitJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_FCMPLT:
			this.visitInsn(Opcodes.FCMPL);
			this.visitJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_FCMPGE:
			this.visitInsn(Opcodes.FCMPG);
			this.visitJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_FCMPGT:
			this.visitInsn(Opcodes.FCMPG);
			this.visitJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_FCMPLE:
			this.visitInsn(Opcodes.FCMPL);
			this.visitJumpInsn(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_DCMPEQ:
			this.visitInsn(Opcodes.DCMPL);
			this.visitJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_DCMPNE:
			this.visitInsn(Opcodes.DCMPL);
			this.visitJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_DCMPLT:
			this.visitInsn(Opcodes.DCMPL);
			this.visitJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_DCMPGE:
			this.visitInsn(Opcodes.DCMPG);
			this.visitJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_DCMPGT:
			this.visitInsn(Opcodes.DCMPG);
			this.visitJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_DCMPLE:
			this.visitInsn(Opcodes.DCMPL);
			this.visitJumpInsn(Opcodes.IFLE, dest);
			return;
		}
	}
	
	@Override
	public void visitTypeInsn(int opcode, String type)
	{
		if (opcode == NEW)
		{
			this.push(type);
		}
		this.mv.visitTypeInsn(opcode, type);
	}
	
	public void visitTypeInsn(int opcode, IType type)
	{
		if ((opcode == ANEWARRAY || opcode == NEWARRAY) && type instanceof PrimitiveType)
		{
			this.mv.visitIntInsn(NEWARRAY, ((PrimitiveType) type).typecode);
			return;
		}
		if (opcode == NEW)
		{
			this.push(type);
		}
		if (opcode == CHECKCAST)
		{
			this.set(type.getFrameType());
		}
		this.mv.visitTypeInsn(opcode, type.getInternalName());
	}
	
	@Override
	public void visitMultiANewArrayInsn(String type, int dims)
	{
		this.push(type);
		this.mv.visitMultiANewArrayInsn(type, dims);
	}
	
	public void visitMultiANewArrayInsn(IType type, int dims)
	{
		this.push(type);
		this.mv.visitMultiANewArrayInsn(type.getExtendedName(), dims);
	}
	
	@Override
	public void visitVarInsn(int opcode, int index)
	{
		if (opcode >= ILOAD && opcode <= ALOAD)
		{
			this.push(this.locals[index]);
		}
		else if (opcode >= ISTORE && opcode <= ASTORE)
		{
			this.pop();
		}
		this.mv.visitVarInsn(opcode, index);
	}
	
	public void visitVarInsn(int opcode, int index, IType type)
	{
		if (type != null)
		{
			this.push(type);
		}
		else
		{
			this.pop();
		}
		this.mv.visitVarInsn(opcode, index);
	}
	
	@Override
	@Deprecated
	public void visitFieldInsn(int opcode, String owner, String name, String desc)
	{
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	public void visitGetStatic(String owner, String name, String desc, IType type)
	{
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitFieldInsn(GETSTATIC, owner, name, desc);
	}
	
	public void visitPutStatic(String owner, String name, String desc)
	{
		this.pop(); // Value
		this.mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
	}
	
	public void visitGetField(String owner, String name, String desc, IType type)
	{
		this.pop(); // Instance
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitFieldInsn(GETFIELD, owner, name, desc);
	}
	
	public void visitPutField(String owner, String name, String desc)
	{
		this.pop(); // Instance
		this.pop(); // Value
		this.mv.visitFieldInsn(PUTFIELD, owner, name, desc);
	}
	
	@Override
	@Deprecated
	public void visitMethodInsn(int opcode, String owner, String name, String desc)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, false);
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, int args, IType returnType)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, false);
		for (int i = 0; i < args; i++)
		{
			this.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	@Deprecated
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, IType returnType)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
		for (int i = 0; i < args; i++)
		{
			this.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, Object returnType)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
		for (int i = 0; i < args; i++)
		{
			this.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	@Deprecated
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs)
	{
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	
	public void visitInvokeDynamicInsn(String name, String desc, int args, IType returnType, Handle bsm, Object... bsmArgs)
	{
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		for (int i = 0; i < args; i++)
		{
			this.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	public void visitInvokeDynamicInsn(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs)
	{
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		for (int i = 0; i < args; i++)
		{
			this.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	public void visitEnd()
	{
		this.mv.visitMaxs(this.maxStack, this.maxLocals);
		this.mv.visitEnd();
	}
	
	public void visitEnd(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			iclass.writeInnerClassInfo(this.cw);
		}
		
		if (!this.hasReturn)
		{
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(this.maxStack, this.maxLocals);
		this.mv.visitEnd();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MethodWriter {stack=[");
		
		for (int i = 0; i < this.stackCount; i++)
		{
			builder.append(typeToString(this.stack[i])).append(", ");
		}
		
		builder.append("], maxStack=").append(this.maxStack);
		builder.append(", locals=[");
		
		for (int i = 0; i < this.localCount; i++)
		{
			builder.append(typeToString(this.locals[i])).append(", ");
		}
		
		builder.append("], maxLocals=").append(this.maxLocals);
		builder.append("}");
		return builder.toString();
	}
	
	private static String typeToString(Object type)
	{
		if (type == TOP)
		{
			return "top";
		}
		if (type == NULL)
		{
			return "null";
		}
		if (type == INTEGER)
		{
			return "int";
		}
		if (type == LONG)
		{
			return "long";
		}
		if (type == FLOAT)
		{
			return "float";
		}
		if (type == DOUBLE)
		{
			return "double";
		}
		if (type == UNINITIALIZED_THIS)
		{
			return "this";
		}
		if (type == null)
		{
			return "[null]";
		}
		return type.toString();
	}
}
