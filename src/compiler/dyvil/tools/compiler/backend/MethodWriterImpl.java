package dyvil.tools.compiler.backend;

import static dyvil.reflect.Opcodes.*;
import org.objectweb.asm.*;
import org.objectweb.asm.ClassWriter;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;

public final class MethodWriterImpl implements MethodWriter
{
	public static final Long	LONG_MINUS_ONE	= Long.valueOf(-1);
	
	public ClassWriter			cw;
	protected MethodVisitor mv;
	
	private boolean				visitFrame;
	private boolean				hasReturn;
	
	private int					localIndex;
	private int					localCount;
	private int					maxLocals;
	private Object[]			locals			= new Object[2];
	
	private int					stackIndex;
	private int					stackCount;
	private int					maxStack;
	private Object[]			stack			= new Object[3];
	
	public MethodWriterImpl(ClassWriter cw, MethodVisitor mv)
	{
		this.cw = cw;
		this.mv = mv;
	}
	
	@Override
	public void setConstructor(IType type)
	{
		this.locals[0] = UNINITIALIZED_THIS;
		this.push(UNINITIALIZED_THIS);
	}
	
	@Override
	public void begin()
	{
		this.mv.visitCode();
	}
	
	@Override
	public AnnotationVisitor addAnnotation(String type, boolean visible)
	{
		return this.mv.visitAnnotation(type, visible);
	}

	@Override
	public AnnotationVisitor addParameterAnnotation(int index, String type, boolean visible)
	{
		return this.mv.visitParameterAnnotation(index, type, visible);
	}
	
	// Stack
	
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
	
	@Override
	public void set(Object type)
	{
		this.stack[this.stackIndex - 1] = type;
	}
	
	@Override
	public void set(IType type)
	{
		this.stack[this.stackIndex - 1] = type.getFrameType();
	}
	
	@Override
	public void push(Object type)
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
	
	@Override
	public void push(IType type)
	{
		Object frameType = type.getFrameType();
		if (frameType != null)
		{
			this.push(frameType);
		}
	}
	
	@Override
	public void pop()
	{
		this.stackIndex--;
		this.stackCount--;
	}
	
	@Override
	public void pop(int count)
	{
		this.stackIndex -= count;
		this.stackCount -= count;
	}
	
	@Override
	public Object peek()
	{
		return this.stack[this.stackIndex];
	}
	
	private void visitFrame()
	{
		this.mv.visitFrame(org.objectweb.asm.Opcodes.F_NEW, this.localCount, this.locals, this.stackCount, this.stack);
		this.visitFrame = false;
	}
	
	// Parameters
	
	@Override
	public int registerParameter(String name, IType type)
	{
		int index = this.registerLocal(type.getFrameType());
		this.mv.visitParameter(name, index);
		
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			iclass.writeInnerClassInfo(this.cw);
		}
		return index;
	}
	
	@Override
	public int registerParameter(String name, Object type)
	{
		int index = this.registerLocal(type);
		this.mv.visitParameter(name, index);
		return index;
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
	
	@Override
	public int localCount()
	{
		return this.localCount;
	}

	@Override
	public int registerLocal(IType type)
	{
		return this.registerLocal(type.getFrameType());
	}

	@Override
	public int registerLocal(Object type)
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

	@Override
	public void removeLocals(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.localCount--;
			Object o = this.locals[--this.localIndex];
			if (o == LONG || o == DOUBLE)
			{
				this.localIndex--;
			}
		}
	}

	@Override
	public void writeLocal(String name, String desc, String signature, Label start, Label end, int index)
	{
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}
	
	@Override
	public void writeLocal(String name, IType type, Label start, Label end, int index)
	{
		this.mv.visitLocalVariable(name, type.getExtendedName(), type.getSignature(), start, end, index);
	}
	
	// Constants
	
	@Override
	public void writeLDC(int value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.push(INT);
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
	
	@Override
	public void writeLDC(long value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
	
	@Override
	public void writeLDC(float value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
	
	@Override
	public void writeLDC(double value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
	
	@Override
	public void writeLDC(String value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.push("Ljava/lang/String;");
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(Type type)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.push("Ljava/lang/Class;");
		this.mv.visitLdcInsn(type);
	}
	
	// Labels
	
	@Override
	public void writeFrameLabel(Label label)
	{
		this.visitFrame = true;
		this.mv.visitLabel(label);
	}
	
	@Override
	public void writeLabel(Label label)
	{
		this.mv.visitLabel(label);
	}
	
	// Other Instructions
	
	@Override
	public void writeInsn(int opcode)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
			this.push(INT);
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
			this.set(INT);
			return;
		case RETURN:
			this.hasReturn = true;
			return;
		case ATHROW:
			this.pop();
			return;
		case BALOAD:
		case SALOAD:
		case CALOAD:
		case IALOAD:
			this.pop();
			this.pop();
			this.push(INT);
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
			this.push(INT);
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
			this.set(INT);
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
			this.push(INT);
			return;
		}
	}
	
	private void visitSpecialInsn(int opcode)
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
			this.set(INT);
			return;
		case Opcodes.L2S:
			this.mv.visitInsn(Opcodes.L2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INT);
			return;
		case Opcodes.L2C:
			this.mv.visitInsn(Opcodes.L2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INT);
			return;
		case Opcodes.F2B:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2B);
			this.set(INT);
			return;
		case Opcodes.F2S:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INT);
			return;
		case Opcodes.F2C:
			this.mv.visitInsn(Opcodes.F2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INT);
			return;
		case Opcodes.D2B:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2B);
			this.set(INT);
			return;
		case Opcodes.D2S:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2S);
			this.set(INT);
			return;
		case Opcodes.D2C:
			this.mv.visitInsn(Opcodes.D2I);
			this.mv.visitInsn(Opcodes.I2C);
			this.set(INT);
			return;
		}
	}
	
	// Jump Instructions
	
	@Override
	public void writeFrameJump(int opcode, Label label)
	{
		if (opcode > 255)
		{
			if (this.visitFrame)
			{
				this.visitFrame();
			}
			
			this.visitSpecialJumpInsn(opcode, label);
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
		if (opcode == GOTO || opcode == JSR || opcode == ATHROW)
		{
			this.visitFrame();
			this.visitFrame = true;
		}
		this.mv.visitJumpInsn(opcode, label);
	}
	
	@Override
	public void writeJump(int opcode, Label label)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_LCMPNE:
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_LCMPLT:
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_LCMPGE:
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_LCMPGT:
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_LCMPLE:
			this.writeInsn(Opcodes.LCMP);
			this.writeFrameJump(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_FCMPEQ:
			this.writeInsn(Opcodes.FCMPL);
			this.writeFrameJump(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_FCMPNE:
			this.writeInsn(Opcodes.FCMPL);
			this.writeFrameJump(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_FCMPLT:
			this.writeInsn(Opcodes.FCMPL);
			this.writeFrameJump(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_FCMPGE:
			this.writeInsn(Opcodes.FCMPG);
			this.writeFrameJump(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_FCMPGT:
			this.writeInsn(Opcodes.FCMPG);
			this.writeFrameJump(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_FCMPLE:
			this.writeInsn(Opcodes.FCMPL);
			this.writeFrameJump(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_DCMPEQ:
			this.writeInsn(Opcodes.DCMPL);
			this.writeFrameJump(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_DCMPNE:
			this.writeInsn(Opcodes.DCMPL);
			this.writeFrameJump(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_DCMPLT:
			this.writeInsn(Opcodes.DCMPL);
			this.writeFrameJump(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_DCMPGE:
			this.writeInsn(Opcodes.DCMPG);
			this.writeFrameJump(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_DCMPGT:
			this.writeInsn(Opcodes.DCMPG);
			this.writeFrameJump(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_DCMPLE:
			this.writeInsn(Opcodes.DCMPL);
			this.writeFrameJump(Opcodes.IFLE, dest);
			return;
		}
	}
	
	@Override
	public void writeTypeInsn(int opcode, String type)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		if (opcode == ANEWARRAY || opcode == NEWARRAY)
		{
			this.push("[" + type);
		}
		if (opcode == NEW)
		{
			this.push(type);
		}
		if (opcode == CHECKCAST) {
			this.set(type);
		}
		this.mv.visitTypeInsn(opcode, type);
	}
	
	@Override
	public void writeTypeInsn(int opcode, IType type)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		if (opcode == ANEWARRAY || opcode == NEWARRAY)
		{
			this.push("[" + type.getExtendedName());
			if (type instanceof PrimitiveType)
			{
				this.mv.visitIntInsn(NEWARRAY, ((PrimitiveType) type).typecode);
				return;
			}
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
	public void writeNewArray(String type, int dims)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.push(type);
		this.mv.visitMultiANewArrayInsn(type, dims);
	}
	
	@Override
	public void writeNewArray(IType type, int dims)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.push(type);
		this.mv.visitMultiANewArrayInsn(type.getExtendedName(), dims);
	}
	
	@Override
	public void writeIINC(int var, int value)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitIincInsn(var, value);
	}
	
	@Override
	public void writeVarInsn(int opcode, int index)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
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
	
	@Override
	public void writeGetStatic(String owner, String name, String desc, IType type)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitFieldInsn(GETSTATIC, owner, name, desc);
	}
	
	@Override
	public void writePutStatic(String owner, String name, String desc)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.pop(); // Value
		this.mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
	}
	
	@Override
	public void writeGetField(String owner, String name, String desc, IType type)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.pop(); // Instance
		this.push(type);
		this.mv.visitFieldInsn(GETFIELD, owner, name, desc);
	}
	
	@Override
	public void writePutField(String owner, String name, String desc)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.pop(); // Instance
		this.pop(); // Value
		this.mv.visitFieldInsn(PUTFIELD, owner, name, desc);
	}
	
	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, Object returnType)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		this.mv.visitMethodInsn(opcode, owner, name, desc, false);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}

	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, int args, IType returnType)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, false);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, Object returnType)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}

	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface, int args, IType returnType)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	public void writeInvokeDynamic(String name, String desc, int args, IType returnType, Handle bsm, Object... bsmArgs)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	public void writeInvokeDynamic(String name, String desc, int args, Object returnType, Handle bsm, Object... bsmArgs)
	{
		if (this.visitFrame)
		{
			this.visitFrame();
		}
		
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		this.pop(args);
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	public void end()
	{
		this.mv.visitMaxs(this.maxStack, this.maxLocals);
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
			if (this.visitFrame)
				this.visitFrame();
			
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
		if (type == INT)
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
