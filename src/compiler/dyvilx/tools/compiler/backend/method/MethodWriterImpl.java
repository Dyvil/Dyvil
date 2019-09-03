package dyvilx.tools.compiler.backend.method;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.*;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dyvil.reflect.Opcodes.*;

public final class MethodWriterImpl implements MethodWriter
{
	private static final Long LONG_MINUS_ONE = -1L;

	public    dyvilx.tools.compiler.backend.classes.ClassWriter cw;
	protected MethodVisitor                                     mv;

	protected Frame frame = new Frame();
	private boolean visitFrame;

	private boolean hasReturn;

	private List<Consumer<? super MethodWriter>> preReturnHandlers;

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
	public boolean visitCode()
	{
		return this.mv.visitCode();
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

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.mv.visitInsnAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.mv.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
		                                                     int[] index, String desc, boolean visible)
	{
		return this.mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault()
	{
		return this.mv.visitAnnotationDefault();
	}

	@Override
	public void visitAttribute(Attribute attr)
	{
		this.mv.visitAttribute(attr);
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
	public int visitParameter(int index, String name, IType type, int access)
	{
		this.mv.visitParameter(name, access);

		this.frame.setLocal(index, type.getFrameType());
		return this.frame.localCount;
	}

	@Override
	public void visitParameter(String name, int access)
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
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}

	// Constants

	@Override
	public void visitLdcInsn(int value)
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
	public void visitLdcInsn(long value)
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
	public void visitLdcInsn(float value)
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
	public void visitLdcInsn(double value)
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
	public void visitLdcInsn(String value)
	{
		this.insnCallback();

		this.frame.push("java/lang/String");

		this.mv.visitLdcInsn(value);
	}

	@Override
	public void visitLdcInsn(Type type)
	{
		this.insnCallback();

		this.frame.push("java/lang/Class");

		this.mv.visitLdcInsn(type);
	}

	@Override
	public void visitLdcInsn(Object cst)
	{
		Class c = cst.getClass();
		if (c == Integer.class)
		{
			this.visitLdcInsn((int) cst);
			return;
		}
		if (c == Long.class)
		{
			this.visitLdcInsn((long) cst);
			return;
		}
		if (c == Float.class)
		{
			this.visitLdcInsn((float) cst);
			return;
		}
		if (c == Double.class)
		{
			this.visitLdcInsn((double) cst);
			return;
		}
		if (c == String.class)
		{
			this.visitLdcInsn((String) cst);
			return;
		}
		if (c == Type.class)
		{
			this.visitLdcInsn((Type) cst);
		}
	}

	// Labels

	@Override
	public void visitLabel(Label label)
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
	public void visitTargetLabel(Label label)
	{
		this.visitLabel(label);
		this.visitFrame = true;
		this.hasReturn = false;
	}

	@Override
	public void visitLineNumber(int line, Label start)
	{
		this.mv.visitLineNumber(line, start);
	}

	// Other Instructions

	@Override
	public void visitInsnAtLine(int opcode, int lineNumber) throws BytecodeException
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
			this.visitLineNumber(lineNumber);
		}

		this.visitInsn(opcode);
	}

	@Override
	public void visitInsn(int opcode) throws BytecodeException
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
			case IS_NULL:
				this.writeBoolJump(IFNULL);
				return;
			case IS_NONNULL:
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
				this.swap();
				return;
			case Opcodes.AUTO_POP:
				this.pop();
				return;
			case Opcodes.AUTO_DUP:
				this.dup();
				return;
			case Opcodes.AUTO_DUP_X1:
				this.dupX1();
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

		if (opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW)
		{
			this.visitReturnInsn(opcode);
			return;
		}

		this.insnCallback();
		this.frame.visitInsn(opcode);
		this.mv.visitInsn(opcode);
	}

	private void visitReturnInsn(int opcode)
	{
		final List<Consumer<? super MethodWriter>> preReturnHandlers = this.preReturnHandlers;
		if (preReturnHandlers != null && !preReturnHandlers.isEmpty())
		{
			// return or throw within the handlers are not handled again
			this.preReturnHandlers = null;
			for (final Consumer<? super MethodWriter> handler : preReturnHandlers)
			{
				handler.accept(this);
			}
			this.preReturnHandlers = preReturnHandlers;

			if (this.hasReturn())
			{
				return;
			}
		}

		this.insnCallback();
		this.frame.visitInsn(opcode);
		this.mv.visitInsn(opcode);

		this.visitFrame = true;
		this.hasReturn = true;
	}

	private void writeBoolJump(int jump) throws BytecodeException
	{
		Label label1 = new Label();
		Label label2 = new Label();

		this.visitJumpInsn(jump, label1);

		this.visitInsn(Opcodes.ICONST_0);
		this.visitJumpInsn(Opcodes.GOTO, label2);
		this.visitTargetLabel(label1);
		this.visitInsn(Opcodes.ICONST_1);
		this.visitTargetLabel(label2);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) throws BytecodeException
	{
		this.insnCallback();

		this.frame.visitIntInsn(opcode, operand);

		this.mv.visitIntInsn(opcode, operand);
	}

	// Jump Instructions

	@Override
	public void visitJumpInsn(int opcode, Label target) throws BytecodeException
	{
		if (opcode > 255)
		{
			switch (opcode)
			{
			case Opcodes.IF_LCMPEQ:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_LCMPNE:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_LCMPLT:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_LCMPGE:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_LCMPGT:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_LCMPLE:
				this.visitInsn(Opcodes.LCMP);
				this.visitJumpInsn(Opcodes.IFLE, target);
				return;
			case Opcodes.IF_FCMPEQ:
				this.visitInsn(Opcodes.FCMPL);
				this.visitJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_FCMPNE:
				this.visitInsn(Opcodes.FCMPL);
				this.visitJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_FCMPLT:
				this.visitInsn(Opcodes.FCMPL);
				this.visitJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_FCMPGE:
				this.visitInsn(Opcodes.FCMPG);
				this.visitJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_FCMPGT:
				this.visitInsn(Opcodes.FCMPG);
				this.visitJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_FCMPLE:
				this.visitInsn(Opcodes.FCMPL);
				this.visitJumpInsn(Opcodes.IFLE, target);
				return;
			case Opcodes.IF_DCMPEQ:
				this.visitInsn(Opcodes.DCMPL);
				this.visitJumpInsn(Opcodes.IFEQ, target);
				return;
			case Opcodes.IF_DCMPNE:
				this.visitInsn(Opcodes.DCMPL);
				this.visitJumpInsn(Opcodes.IFNE, target);
				return;
			case Opcodes.IF_DCMPLT:
				this.visitInsn(Opcodes.DCMPL);
				this.visitJumpInsn(Opcodes.IFLT, target);
				return;
			case Opcodes.IF_DCMPGE:
				this.visitInsn(Opcodes.DCMPG);
				this.visitJumpInsn(Opcodes.IFGE, target);
				return;
			case Opcodes.IF_DCMPGT:
				this.visitInsn(Opcodes.DCMPG);
				this.visitJumpInsn(Opcodes.IFGT, target);
				return;
			case Opcodes.IF_DCMPLE:
				this.visitInsn(Opcodes.DCMPL);
				this.visitJumpInsn(Opcodes.IFLE, target);
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
	public void visitTypeInsn(int opcode, String type) throws BytecodeException
	{
		this.insnCallback();

		switch (opcode)
		{
		case Opcodes.NEW:
			Label label = new Label();
			this.mv.visitLabel(label);
			this.frame.push(label);
			break;
		case Opcodes.CHECKCAST:
			if (type.equals(this.frame.peek()))
			{
				// Optimization: omit redundant casts
				return;
			}
			this.frame.set(type);
			break;
		default:
			this.frame.visitTypeInsn(opcode, type);
			break;
		}

		this.mv.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitMultiANewArrayInsn(String type, int dims) throws BytecodeException
	{
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
	public void visitMultiANewArrayInsn(IType type, int dims) throws BytecodeException
	{
		this.insnCallback();

		if (dims == 1)
		{
			final ArrayType arrayType = type.extract(ArrayType.class);
			final IType elementType = arrayType.getElementType();

			if (elementType.isPrimitive())
			{
				this.visitIntInsn(Opcodes.NEWARRAY, getNewArrayCode(elementType.getTypecode()));
				return;
			}

			this.visitTypeInsn(Opcodes.ANEWARRAY, elementType.getInternalName());
			return;
		}

		final String extended = type.getExtendedName();
		this.frame.visitNewArray(extended, dims);

		this.mv.visitMultiANewArrayInsn(extended, dims);
	}

	@Override
	public void visitIincInsn(int index, int value) throws BytecodeException
	{
		this.insnCallback();

		this.mv.visitIincInsn(index, value);
	}

	@Override
	public void visitVarInsn(int opcode, int index) throws BytecodeException
	{
		this.insnCallback();

		if (opcode == AUTO_LOAD)
		{
			final PrimitiveType primitiveType = PrimitiveType.fromFrameType(this.frame.locals[index]);
			opcode = primitiveType != null ? primitiveType.getLoadOpcode() : Opcodes.ALOAD;
		}
		else if (opcode == AUTO_STORE)
		{
			final PrimitiveType primitiveType = PrimitiveType.fromFrameType(this.frame.peek());
			opcode = primitiveType != null ? primitiveType.getStoreOpcode() : Opcodes.ASTORE;
		}

		this.frame.visitVarInsn(opcode, index);

		this.mv.visitVarInsn(opcode, index);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc, Object fieldType)
		throws BytecodeException
	{
		this.insnCallback();

		this.frame.visitFieldInsn(opcode, fieldType);

		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, int args, Object returnType,
		                           boolean isInterface) throws BytecodeException
	{
		this.insnCallback();

		this.frame.visitInvokeInsn(args, returnType);

		if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && this.frame.stackCount > 0)
		{
			this.frame.set(owner);
		}

		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, int args, Object returnType, Handle bsm,
		                                  Object... bsmArgs) throws BytecodeException
	{
		this.insnCallback();

		this.frame.visitInvokeInsn(args, returnType);

		this.mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	// Switch Instructions

	@Override
	public void visitTableSwitchInsn(int start, int end, Label defaultHandler, Label... handlers)
		throws BytecodeException
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
	public void visitLookupSwitchInsn(Label defaultHandler, int[] keys, Label[] handlers) throws BytecodeException
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

	// Blocks

	@Override
	public void addPreReturnHandler(Consumer<? super MethodWriter> handler)
	{
		if (this.preReturnHandlers == null)
		{
			this.preReturnHandlers = new ArrayList<>();
		}
		this.preReturnHandlers.add(handler);
	}

	@Override
	public void removePreReturnHandler(Consumer<? super MethodWriter> handler)
	{
		if (this.preReturnHandlers != null)
		{
			this.preReturnHandlers.add(handler);
		}
	}

	@Override
	public void startCatchBlock(String type)
	{
		this.frame.push(type);
	}

	@Override
	public void visitFinallyBlock(Label start, Label end, Label handler)
	{
		this.mv.visitTryCatchBlock(start, end, handler, null);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
	{
		this.mv.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitEnd()
	{
		this.mv.visitMaxs(this.frame.maxStack, this.frame.maxLocals);
		this.mv.visitEnd();
	}

	@Override
	public void visitEnd(IType type)
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

	void swap() throws BytecodeException
	{
		Object t1 = this.frame.pop();
		Object t2 = this.frame.pop();

		this.frame.push(t1);
		this.frame.push(t2);

		if (ClassFormat.isTwoWord(t2))
		{
			if (ClassFormat.isTwoWord(t1))
			{
				// { value4, value3 }, { value2, value1 } ->
				// { value2, value1 }, { value4, value3 }
				this.mv.visitInsn(DUP2_X2);
				this.mv.visitInsn(POP2);
			}
			else
			{
				// { value3, value2 }, value1 ->
				// value1, { value3, value2 }
				this.mv.visitInsn(DUP_X2);
				this.mv.visitInsn(POP);
			}
		}
		else
		{
			if (ClassFormat.isTwoWord(t1))
			{
				// value3, { value2, value1 } ->
				// { value2, value1 }, value3
				this.mv.visitInsn(DUP2_X1);
				this.mv.visitInsn(POP2);
			}
			else
			{
				// value2, value1 -> value1, value1
				this.mv.visitInsn(SWAP);
			}
		}
	}

	void pop() throws BytecodeException
	{
		Object t = this.frame.pop();

		if (ClassFormat.isTwoWord(t))
		{
			// { value2, value1 } ->
			this.mv.visitInsn(POP2);
		}
		else
		{
			// value1 ->
			this.mv.visitInsn(POP);
		}
	}

	void dup()
	{
		Object t = this.frame.peek();
		this.frame.push(t);
		if (ClassFormat.isTwoWord(t))
		{
			// { value2, value1 } -> { value2, value1 }, { value2, value1 }
			this.mv.visitInsn(DUP2);
		}
		else
		{
			// value1 -> value1, value1
			this.mv.visitInsn(DUP);
		}
	}

	void dupX1() throws BytecodeException
	{
		Object t1 = this.frame.pop();
		Object t2 = this.frame.pop();

		this.frame.push(t1);
		this.frame.push(t2);
		this.frame.push(t1);
		if (ClassFormat.isTwoWord(t1))
		{
			if (ClassFormat.isTwoWord(t2))
			{
				// { value4, value3 }, { value2, value1 } ->
				// { value2, value1 }, { value4, value3 }, { value2, value1 }
				this.mv.visitInsn(DUP2_X2);
			}
			else
			{
				// value3, { value2, value1 } ->
				// { value2, value1 }, value3, { value2, value1 }
				this.mv.visitInsn(DUP2_X1);
			}
		}
		else
		{
			if (ClassFormat.isTwoWord(t2))
			{
				// { value3, value2 }, value1 ->
				// value1, { value3, value2 }, value1
				this.mv.visitInsn(DUP_X2);
			}
			else
			{
				// value2, value1 -> value1, value2, value1
				this.mv.visitInsn(DUP_X1);
			}
		}
	}
}
