package dyvil.tools.compiler.backend;

import static dyvil.reflect.Opcodes.*;

import org.objectweb.asm.*;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;

public final class MethodWriterImpl implements MethodWriter
{
	private static final Long	LONG_MINUS_ONE	= Long.valueOf(-1);
	
	public ClassWriter			cw;
	protected MethodVisitor		mv;
	
	private boolean				hasReturn;
	private int					localIndex;
	private int					inlineOffset;
	private Label				inlineEnd;
	
	private int[]				syncLocals;
	private int					syncCount;
	
	public MethodWriterImpl(ClassWriter cw, MethodVisitor mv)
	{
		this.cw = cw;
		this.mv = mv;
	}
	
	@Override
	public ClassWriter getClassWriter()
	{
		return this.cw;
	}
	
	@Override
	public void setInstanceMethod()
	{
		this.localIndex = 1;
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
	
	public void insnCallback()
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.mv.visitJumpInsn(Opcodes.GOTO, this.inlineEnd);
			this.hasReturn = false;
		}
	}
	
	// Parameters
	
	@Override
	public int registerParameter(int index, String name, IType type, int access)
	{
		this.mv.visitParameter(name, access);
		
		if (type == Types.LONG || type == Types.DOUBLE)
		{
			return this.localIndex = index + 2;
		}
		
		return this.localIndex = index + 1;
	}
	
	@Override
	public void registerParameter(String name, int access)
	{
		this.mv.visitParameter(name, access);
	}
	
	// Locals
	
	@Override
	public int registerLocal()
	{
		return this.localIndex;
	}
	
	@Override
	public void resetLocals(int count)
	{
		this.localIndex = count;
	}
	
	@Override
	public void writeLocal(int index, String name, String desc, String signature, Label start, Label end)
	{
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}
	
	@Override
	public void writeLocal(int index, String name, IType type, Label start, Label end)
	{
		this.mv.visitLocalVariable(name, type.getExtendedName(), type.getSignature(), start, end, index);
	}
	
	// Constants
	
	@Override
	public void writeLDC(int value)
	{
		this.insnCallback();
		
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
		this.mv.visitLdcInsn(Integer.valueOf(value));
	}
	
	@Override
	public void writeLDC(long value)
	{
		this.insnCallback();
		
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
		this.insnCallback();
		
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
		this.insnCallback();
		
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
		this.insnCallback();
		
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(Type type)
	{
		this.insnCallback();
		
		this.mv.visitLdcInsn(type);
	}
	
	// Labels
	
	@Override
	public void writeLabel(Label label)
	{
		this.insnCallback();
		
		this.mv.visitLabel(label);
	}
	
	// Other Instructions
	
	@Override
	public void writeInsn(int opcode)
	{
		this.insnCallback();
		
		if (opcode > 255)
		{
			switch (opcode)
			{
			case Opcodes.LCONST_M1:
				this.mv.visitLdcInsn(LONG_MINUS_ONE);
				return;
			case Opcodes.BINV:
			{
				Label label1 = new Label();
				Label label2 = new Label();
				this.mv.visitJumpInsn(Opcodes.IFEQ, label1);
				this.mv.visitInsn(Opcodes.ICONST_0);
				this.mv.visitJumpInsn(Opcodes.GOTO, label2);
				this.mv.visitLabel(label1);
				this.mv.visitInsn(Opcodes.ICONST_1);
				this.mv.visitLabel(label2);
			}
			case Opcodes.IINV:
				this.mv.visitInsn(Opcodes.ICONST_M1);
				this.mv.visitInsn(Opcodes.IXOR);
				return;
			case Opcodes.LINV:
				this.mv.visitLdcInsn(LONG_MINUS_ONE);
				this.mv.visitInsn(Opcodes.IXOR);
				return;
			case Opcodes.L2B:
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.L2S:
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.L2C:
				this.mv.visitInsn(Opcodes.L2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case Opcodes.F2B:
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.F2S:
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.F2C:
				this.mv.visitInsn(Opcodes.F2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case Opcodes.D2B:
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2B);
				return;
			case Opcodes.D2S:
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2S);
				return;
			case Opcodes.D2C:
				this.mv.visitInsn(Opcodes.D2I);
				this.mv.visitInsn(Opcodes.I2C);
				return;
			case Opcodes.OBJECT_EQUALS:
				this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
				return;
			}
			if (opcode >= ICMPEQ && opcode <= ICMPLE)
			{
				opcode -= ICMPEQ;
				
				Label label1 = new Label();
				Label label2 = new Label();
				this.mv.visitJumpInsn(Opcodes.IF_ICMPEQ + opcode, label1);
				this.mv.visitInsn(Opcodes.ICONST_0);
				this.mv.visitJumpInsn(Opcodes.GOTO, label2);
				this.mv.visitLabel(label1);
				this.mv.visitInsn(Opcodes.ICONST_1);
				this.mv.visitLabel(label2);
			}
			return;
		}
		if (opcode >= IRETURN && opcode <= RETURN)
		{
			if (this.syncCount > 0)
			{
				for (int i = 0; i < this.syncCount; i++)
				{
					this.mv.visitVarInsn(Opcodes.ALOAD, this.syncLocals[i]);
					this.mv.visitInsn(Opcodes.MONITOREXIT);
				}
			}
			this.hasReturn = true;
			if (this.inlineEnd != null)
			{
				return;
			}
		}
		this.mv.visitInsn(opcode);
	}
	
	@Override
	public void writeIntInsn(int opcode, int operand)
	{
		this.insnCallback();
		
		this.mv.visitIntInsn(opcode, operand);
	}
	
	// Jump Instructions
	
	@Override
	public void writeJumpInsn(int opcode, Label target)
	{
		this.insnCallback();
		
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
		this.mv.visitJumpInsn(opcode, target);
	}
	
	@Override
	public void writeTypeInsn(int opcode, String type)
	{
		this.insnCallback();
		
		this.mv.visitTypeInsn(opcode, type);
	}
	
	@Override
	public void writeNewArray(String type, int dims)
	{
		this.insnCallback();
		
		if (dims == 1)
		{
			this.writeTypeInsn(Opcodes.ANEWARRAY, type);
			return;
		}
		
		this.mv.visitMultiANewArrayInsn(type, dims);
	}
	
	@Override
	public void writeNewArray(IType type, int dims)
	{
		this.insnCallback();
		
		if (dims == 1)
		{
			if (type instanceof PrimitiveType)
			{
				this.mv.visitIntInsn(Opcodes.NEWARRAY, ((PrimitiveType) type).typecode);
				return;
			}
			
			this.mv.visitTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
			return;
		}
		
		this.mv.visitMultiANewArrayInsn(type.getExtendedName(), dims);
	}
	
	@Override
	public void writeIINC(int var, int value)
	{
		this.insnCallback();
		
		this.mv.visitIincInsn(var, value);
	}
	
	@Override
	public void writeVarInsn(int opcode, int index)
	{
		this.insnCallback();
		
		if (index >= this.localIndex)
		{
			if (opcode == ISTORE || opcode == FSTORE || opcode == ASTORE)
			{
				this.localIndex = index + 1;
			}
			else if (opcode == LSTORE || opcode == DSTORE)
			{
				this.localIndex = index + 2;
			}
		}
		this.mv.visitVarInsn(opcode, index);
	}
	
	@Override
	public void writeFieldInsn(int opcode, String owner, String name, String desc)
	{
		this.insnCallback();
		
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		this.insnCallback();
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}
	
	@Override
	public void writeInvokeDynamic(String name, String desc, Handle bsm, Object... bsmArgs)
	{
		this.insnCallback();
		
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	
	// Switch Instructions
	
	@Override
	public void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers)
	{
		this.insnCallback();
		
		this.mv.visitTableSwitchInsn(start, end, defaultHandler, handlers);
	}
	
	@Override
	public void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers)
	{
		this.insnCallback();
		
		this.mv.visitLookupSwitchInsn(defaultHandler, keys, handlers);
	}
	
	// Inlining
	
	@Override
	public int inlineOffset()
	{
		return this.inlineOffset;
	}
	
	@Override
	public void startInline(Label end, int localCount)
	{
		this.inlineEnd = end;
		this.inlineOffset = localCount;
	}
	
	@Override
	public void endInline(Label end, int localCount)
	{
		this.mv.visitLabel(end);
		this.inlineEnd = null;
		this.inlineOffset = 0;
		this.hasReturn = false;
	}
	
	@Override
	public int startSync()
	{
		if (this.syncLocals == null)
		{
			this.syncLocals = new int[1];
			this.syncCount = 1;
			return this.syncLocals[0] = this.localIndex;
		}
		
		int index = this.syncCount++;
		if (index >= this.syncLocals.length)
		{
			int[] temp = new int[this.syncCount];
			System.arraycopy(this.syncLocals, 0, temp, 0, this.syncLocals.length);
			this.syncLocals = temp;
		}
		return this.syncLocals[index] = this.localIndex;
	}
	
	@Override
	public void endSync()
	{
		this.syncCount--;
	}
	
	@Override
	public void writeFinallyBlock(Label start, Label end, Label handler)
	{
		this.mv.visitTryCatchBlock(start, end, handler, null);
	}
	
	@Override
	public void writeTryCatchBlock(Label start, Label end, Label handler, String type)
	{
		this.mv.visitTryCatchBlock(start, end, handler, type);
	}
	
	@Override
	public void end()
	{
		this.mv.visitMaxs(0, 0);
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
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(0, 0);
		this.mv.visitEnd();
	}
	
	@Override
	public String toString()
	{
		return this.mv.toString();
	}
}
