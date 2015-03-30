package dyvil.tools.compiler.backend;

import static dyvil.reflect.Opcodes.*;
import static dyvil.tools.compiler.backend.ClassFormat.DOUBLE;
import static dyvil.tools.compiler.backend.ClassFormat.LONG;

import org.objectweb.asm.*;
import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;

public final class MethodWriterImpl implements MethodWriter
{
	private static final Long	LONG_MINUS_ONE	= Long.valueOf(-1);
	
	public ClassWriter			cw;
	protected MethodVisitor		mv;
	
	private boolean				hasReturn;
	private int					localIndex;
	private Label				inlineEnd;
	
	public MethodWriterImpl(ClassWriter cw, MethodVisitor mv)
	{
		this.cw = cw;
		this.mv = mv;
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
	
	public void writeInlineReturn()
	{
		this.mv.visitJumpInsn(Opcodes.GOTO, this.inlineEnd);
		this.hasReturn = false;
	}
	
	// Parameters
	
	@Override
	public int registerParameter(String name, Object type)
	{
		int index = this.localIndex;
		this.mv.visitParameter(name, index);
		
		if (type == LONG || type == DOUBLE)
		{
			this.localIndex += 2;
		}
		else
		{
			this.localIndex += 1;
		}
		return index;
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	public void writeLDC(Type type)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitLdcInsn(type);
	}
	
	// Labels
	
	@Override
	public void writeLabel(Label label)
	{
		this.mv.visitLabel(label);
	}
	
	// Other Instructions
	
	@Override
	public void writeInsn(int opcode)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		if (opcode > 255)
		{
			this.visitSpecialInsn(opcode);
			return;
		}
		this.mv.visitInsn(opcode);
	}
	
	private void visitSpecialInsn(int opcode)
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
	}
	
	@Override
	public void writeIntInsn(int opcode, int operand)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitIntInsn(opcode, operand);
	}
	
	// Jump Instructions
	
	@Override
	public void writeJumpInsn(int opcode, Label label)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		if (opcode > 255)
		{
			this.visitSpecialJumpInsn(opcode, label);
			return;
		}
		this.mv.visitJumpInsn(opcode, label);
	}
	
	private void visitSpecialJumpInsn(int opcode, Label dest)
	{
		switch (opcode)
		{
		case Opcodes.IF_LCMPEQ:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_LCMPNE:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_LCMPLT:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_LCMPGE:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_LCMPGT:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_LCMPLE:
			this.writeInsn(Opcodes.LCMP);
			this.writeJumpInsn(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_FCMPEQ:
			this.writeInsn(Opcodes.FCMPL);
			this.writeJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_FCMPNE:
			this.writeInsn(Opcodes.FCMPL);
			this.writeJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_FCMPLT:
			this.writeInsn(Opcodes.FCMPL);
			this.writeJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_FCMPGE:
			this.writeInsn(Opcodes.FCMPG);
			this.writeJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_FCMPGT:
			this.writeInsn(Opcodes.FCMPG);
			this.writeJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_FCMPLE:
			this.writeInsn(Opcodes.FCMPL);
			this.writeJumpInsn(Opcodes.IFLE, dest);
			return;
		case Opcodes.IF_DCMPEQ:
			this.writeInsn(Opcodes.DCMPL);
			this.writeJumpInsn(Opcodes.IFEQ, dest);
			return;
		case Opcodes.IF_DCMPNE:
			this.writeInsn(Opcodes.DCMPL);
			this.writeJumpInsn(Opcodes.IFNE, dest);
			return;
		case Opcodes.IF_DCMPLT:
			this.writeInsn(Opcodes.DCMPL);
			this.writeJumpInsn(Opcodes.IFLT, dest);
			return;
		case Opcodes.IF_DCMPGE:
			this.writeInsn(Opcodes.DCMPG);
			this.writeJumpInsn(Opcodes.IFGE, dest);
			return;
		case Opcodes.IF_DCMPGT:
			this.writeInsn(Opcodes.DCMPG);
			this.writeJumpInsn(Opcodes.IFGT, dest);
			return;
		case Opcodes.IF_DCMPLE:
			this.writeInsn(Opcodes.DCMPL);
			this.writeJumpInsn(Opcodes.IFLE, dest);
			return;
		}
	}
	
	@Override
	public void writeTypeInsn(int opcode, String type)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitTypeInsn(opcode, type);
	}
	
	@Override
	public void writeNewArray(String type, int dims)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitMultiANewArrayInsn(type, dims);
	}
	
	@Override
	public void writeNewArray(IType type, int dims)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitMultiANewArrayInsn(type.getExtendedName(), dims);
	}
	
	@Override
	public void writeIINC(int var, int value)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitIincInsn(var, value);
	}
	
	@Override
	public void writeVarInsn(int opcode, int index)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
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
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void writeInvokeInsn(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}
	
	@Override
	public void writeInvokeDynamic(String name, String desc, Handle bsm, Object... bsmArgs)
	{
		if (this.hasReturn && this.inlineEnd != null)
		{
			this.writeInlineReturn();
		}
		this.hasReturn = false;
		
		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	
	// Switch Instructions
	
	@Override
	public void writeTableSwitch(Label defaultHandler, int start, int end, Label[] handlers)
	{
		this.mv.visitTableSwitchInsn(start, end, defaultHandler, handlers);
	}
	
	@Override
	public void writeLookupSwitch(Label defaultHandler, int[] keys, Label[] handlers)
	{
		this.mv.visitLookupSwitchInsn(defaultHandler, keys, handlers);
	}
	
	// Inlining
	
	@Override
	public void startInline(Label end)
	{
		this.inlineEnd = end;
	}
	
	@Override
	public void endInline(Label end)
	{
		this.mv.visitLabel(end);
		this.inlineEnd = null;
		this.hasReturn = false;
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
