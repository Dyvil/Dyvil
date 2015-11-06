package dyvil.tools.compiler.backend;

import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.exception.StackUnderflowException;

import static dyvil.reflect.Opcodes.*;
import static dyvil.tools.compiler.backend.ClassFormat.*;

public class Frame
{
	protected Object[]	stack;
	protected int		stackCount;
	protected int		actualStackCount;
	protected int		maxStack;
	
	protected Object[]	locals;
	protected int		localCount;
	protected int		maxLocals;
	
	public Frame()
	{
		this.stack = new Object[3];
		this.locals = new Object[4];
	}
	
	public Frame(int stackCount, Object[] stack, int localCount, Object[] locals)
	{
		this.stackCount = stackCount;
		this.stack = new Object[stackCount];
		System.arraycopy(stack, 0, this.stack, 0, stackCount);
		
		this.localCount = localCount;
		this.locals = new Object[localCount];
		System.arraycopy(locals, 0, this.locals, 0, localCount);
	}
	
	public static int getArgumentCount(String desc)
	{
		int index = desc.lastIndexOf(')');
		int count = 0;
		for (int i = 1; i < index; i++)
		{
			count++;
			while (desc.charAt(i) == '[')
			{
				i++;
			}
			if (desc.charAt(i) == 'L')
			{
				do
				{
					i++;
				}
				while (desc.charAt(i) != ';');
			}
		}
		return count;
	}
	
	public static Object returnType(String desc)
	{
		int index = desc.lastIndexOf(')') + 1;
		char c = desc.charAt(index);
		switch (c)
		{
		case 'V':
			return null;
		case 'Z':
			return ClassFormat.BOOLEAN;
		case 'B':
			return ClassFormat.BYTE;
		case 'S':
			return ClassFormat.SHORT;
		case 'C':
			return ClassFormat.CHAR;
		case 'I':
			return ClassFormat.INT;
		case 'J':
			return ClassFormat.LONG;
		case 'F':
			return ClassFormat.FLOAT;
		case 'D':
			return ClassFormat.DOUBLE;
		case '[':
			return desc.substring(index);
		case 'L':
			return desc.substring(index + 1, desc.length() - 1);
		default:
			return null;
		}
	}
	
	public static Object fieldType(String desc)
	{
		char c = desc.charAt(0);
		switch (c)
		{
		case 'V':
			return null;
		case 'Z':
			return ClassFormat.BOOLEAN;
		case 'B':
			return ClassFormat.BYTE;
		case 'S':
			return ClassFormat.SHORT;
		case 'C':
			return ClassFormat.CHAR;
		case 'I':
			return ClassFormat.INT;
		case 'J':
			return ClassFormat.LONG;
		case 'F':
			return ClassFormat.FLOAT;
		case 'D':
			return ClassFormat.DOUBLE;
		case '[':
			return desc;
		case 'L':
			return desc.substring(1, desc.length() - 1);
		default:
			return null;
		}
	}
	
	public static String frameTypeName(Object o)
	{
		if (o == BOOLEAN)
		{
			return "boolean";
		}
		if (o == BYTE)
		{
			return "byte";
		}
		if (o == SHORT)
		{
			return "short";
		}
		if (o == CHAR)
		{
			return "char";
		}
		if (o == INT)
		{
			return "int";
		}
		if (o == LONG)
		{
			return "long";
		}
		if (o == FLOAT)
		{
			return "float";
		}
		if (o == DOUBLE)
		{
			return "double";
		}
		if (o == NULL)
		{
			return "null";
		}
		if (o == UNINITIALIZED_THIS)
		{
			return "uninit_this";
		}
		if (o == null)
		{
			return "INVALID";
		}
		return o.toString();
	}
	
	public void setInstance(String type)
	{
		this.localCount = 1;
		this.maxLocals = 1;
		this.locals[0] = type;
	}
	
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
	
	public void setLocal(int index, Object type)
	{
		if (type == LONG || type == DOUBLE)
		{
			this.ensureLocals(index + 2);
			this.locals[index] = type;
			this.locals[index + 1] = TOP;
			return;
		}
		
		this.ensureLocals(index + 1);
		this.locals[index] = type;
	}
	
	private void ensureStack(int count)
	{
		if (count > this.stack.length)
		{
			Object[] newLocals = new Object[count];
			System.arraycopy(this.stack, 0, newLocals, 0, this.stack.length);
			this.stack = newLocals;
		}
		if (this.actualStackCount > this.maxStack)
		{
			this.maxStack = this.actualStackCount;
		}
	}
	
	public int stackCount()
	{
		return this.stackCount;
	}
	
	public void set(Object type)
	{
		if (type == LONG || type == DOUBLE)
		{
			Object o = this.stack[this.stackCount - 1];
			if (o != LONG && o != DOUBLE)
			{
				this.actualStackCount++;
			}
		}
		
		this.stack[this.stackCount - 1] = type;
	}
	
	public void push(Object type)
	{
		if (type == LONG || type == DOUBLE)
		{
			this.actualStackCount += 2;
		}
		else
		{
			this.actualStackCount++;
		}
		
		this.ensureStack(this.stackCount + 1);
		this.stack[this.stackCount++] = type;
	}
	
	public void reserve(int stackSlots)
	{
		if (this.actualStackCount + stackSlots > this.maxStack)
		{
			this.maxStack = this.actualStackCount + stackSlots;
		}
	}
	
	public void pop() throws StackUnderflowException
	{
		if (this.stackCount == 0)
		{
			throw new StackUnderflowException();
		}
		
		Object o = this.stack[--this.stackCount];
		if (o == LONG || o == DOUBLE)
		{
			this.actualStackCount -= 2;
		}
		else
		{
			this.actualStackCount--;
		}
	}
	
	public Object popAndGet() throws StackUnderflowException
	{
		Object o = this.stack[--this.stackCount];
		if (o == LONG || o == DOUBLE)
		{
			this.actualStackCount -= 2;
		}
		else
		{
			this.actualStackCount--;
		}
		return o;
	}
	
	public Object peek()
	{
		return this.stack[this.stackCount - 1];
	}
	
	public void visitInsn(int opcode) throws BytecodeException
	{
		switch (opcode)
		{
		case NOP:
			return;
		case ACONST_NULL:
			this.push(NULL);
			return;
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			this.push(INT);
			return;
		case LCONST_M1:
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
		case BALOAD:
			this.pop();
			this.pop();
			this.push(BYTE);
			return;
		case CALOAD:
			this.pop();
			this.pop();
			this.push(CHAR);
			return;
		case SALOAD:
			this.pop();
			this.pop();
			this.push(SHORT);
			return;
		case IASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case LASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case FASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case DASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case AASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case BASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case CASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case SASTORE:
			this.pop();
			this.pop();
			this.pop();
			return;
		case POP:
			this.pop();
			return;
		case POP2:
			this.pop();
			this.pop();
			return;
		case DUP:
		{
			this.actualStackCount++;
			this.ensureStack(this.stackCount + 1);
			this.stack[this.stackCount] = this.stack[this.stackCount - 1];
			this.stackCount++;
			return;
		}
		case DUP_X1:
		{
			this.actualStackCount++;
			this.ensureStack(this.stackCount + 1);
			this.stack[this.stackCount] = this.stack[this.stackCount - 1];
			this.stack[this.stackCount - 1] = this.stack[this.stackCount - 2];
			this.stack[this.stackCount - 2] = this.stack[this.stackCount];
			this.stackCount++;
			return;
		}
		case DUP_X2:
		{
			this.actualStackCount++;
			this.ensureStack(this.stackCount + 1);
			this.stack[this.stackCount] = this.stack[this.stackCount - 1];
			this.stack[this.stackCount - 1] = this.stack[this.stackCount - 2];
			this.stack[this.stackCount - 2] = this.stack[this.stackCount - 3];
			this.stack[this.stackCount - 3] = this.stack[this.stackCount];
			this.stackCount++;
			return;
		}
		case DUP2:
		{
			Object o = this.stack[this.stackCount - 1];
			if (o == LONG || o == DOUBLE)
			{
				this.actualStackCount += 2;
				this.ensureStack(this.stackCount + 1);
				this.stack[this.stackCount++] = o;
				return;
			}
			
			this.actualStackCount += 2;
			this.ensureStack(this.stackCount + 2);
			this.stack[this.stackCount] = this.stack[this.stackCount - 2];
			this.stack[this.stackCount + 1] = this.stack[this.stackCount - 1];
			this.stackCount += 2;
			return;
		}
		case DUP2_X1:
		{
			this.actualStackCount += 2;
			this.ensureStack(this.stackCount + 2);
			this.stack[this.stackCount] = this.stack[this.stackCount - 3];
			this.stack[this.stackCount + 1] = this.stack[this.stackCount - 2];
			this.stackCount += 2;
			return;
		}
		case DUP2_X2:
		{
			this.actualStackCount += 2;
			this.ensureStack(this.stackCount + 2);
			this.stack[this.stackCount] = this.stack[this.stackCount - 4];
			this.stack[this.stackCount + 1] = this.stack[this.stackCount - 3];
			this.stackCount += 2;
			return;
		}
		case SWAP:
		{
			Object o = this.stack[this.stackCount - 1];
			this.stack[this.stackCount - 1] = this.stack[this.stackCount - 2];
			this.stack[this.stackCount - 2] = o;
			return;
		}
		case IADD:
		case ISUB:
		case IMUL:
		case IDIV:
		case IREM:
		case ISHL:
		case ISHR:
		case IUSHR:
		case IAND:
		case IOR:
		case IXOR:
			this.pop();
			return;
		case LADD:
		case LSUB:
		case LMUL:
		case LDIV:
		case LREM:
		case LSHL:
		case LSHR:
		case LUSHR:
		case LAND:
		case LOR:
		case LXOR:
			this.pop();
			return;
		case FADD:
		case FSUB:
		case FMUL:
		case FDIV:
		case FREM:
			this.pop();
			return;
		case DADD:
		case DSUB:
		case DMUL:
		case DDIV:
		case DREM:
			this.pop();
			return;
		case INEG:
		case LNEG:
		case FNEG:
		case DNEG:
			return;
		case L2I:
		case F2I:
		case D2I:
			this.pop();
			this.push(INT);
			return;
		case I2L:
		case F2L:
		case D2L:
			this.pop();
			this.push(LONG);
			return;
		case I2F:
		case L2F:
		case D2F:
			this.pop();
			this.push(FLOAT);
			return;
		case I2D:
		case L2D:
		case F2D:
			this.pop();
			this.push(DOUBLE);
			return;
		case I2B:
			this.pop();
			this.push(BYTE);
			return;
		case I2C:
			this.pop();
			this.push(CHAR);
			return;
		case I2S:
			this.pop();
			this.push(SHORT);
			return;
		case LCMP:
		case FCMPL:
		case FCMPG:
		case DCMPL:
		case DCMPG:
			this.pop();
			this.pop();
			this.push(INT);
			return;
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
		case ARETURN:
			this.stackCount = this.actualStackCount = 0;
			return;
		case RETURN:
			return;
		case ARRAYLENGTH:
			this.pop();
			this.push(INT);
			return;
		case ATHROW:
		case MONITORENTER:
		case MONITOREXIT:
		case TABLESWITCH:
		case LOOKUPSWITCH:
			this.pop();
			return;
		}
	}
	
	public void visitIntInsn(int opcode, int operand) throws BytecodeException
	{
		switch (opcode)
		{
		case BIPUSH:
			this.push(BYTE);
			return;
		case SIPUSH:
			this.push(SHORT);
			return;
		case NEWARRAY:
			this.pop();
			switch (operand)
			{
			case T_BOOLEAN:
				this.push("[Z");
				return;
			case T_BYTE:
				this.push("[B");
				return;
			case T_SHORT:
				this.push("[S");
				return;
			case T_CHAR:
				this.push("[C");
				return;
			case T_INT:
				this.push("[I");
				return;
			case T_LONG:
				this.push("[J");
				return;
			case T_FLOAT:
				this.push("[F");
				return;
			case T_DOUBLE:
				this.push("[D");
				return;
			default:
				return;
			}
		}
	}
	
	public void visitJumpInsn(int opcode) throws BytecodeException
	{
		switch (opcode)
		{
		case IFEQ:
		case IFNE:
		case IFLT:
		case IFGE:
		case IFGT:
		case IFLE:
			this.pop();
			return;
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ACMPEQ:
		case IF_ACMPNE:
			this.pop();
			this.pop();
			return;
		case GOTO:
			return;
		case IFNULL:
		case IFNONNULL:
			this.pop();
			return;
		}
	}
	
	public void visitTypeInsn(int opcode, String type) throws BytecodeException
	{
		switch (opcode)
		{
		case NEW:
			this.push(type);
			return;
		case ANEWARRAY:
			this.pop();
			this.push('[' + type);
			return;
		case CHECKCAST:
			this.pop();
			this.push(type);
			return;
		case INSTANCEOF:
			this.pop();
			this.push(INT);
			return;
		}
	}
	
	public void visitNewArray(String type, int dims) throws BytecodeException
	{
		for (int i = 0; i < dims; i++)
		{
			this.pop();
		}
		this.push(type);
	}
	
	public void visitVarInsn(int opcode, int index) throws BytecodeException
	{
		switch (opcode)
		{
		case ILOAD:
			this.push(INT);
			return;
		case LLOAD:
			this.push(LONG);
			return;
		case FLOAD:
			this.push(FLOAT);
			return;
		case DLOAD:
			this.push(DOUBLE);
			return;
		case ALOAD:
			this.push(this.locals[index]);
			return;
		case ISTORE:
			this.setLocal(index, INT);
			this.pop();
			return;
		case LSTORE:
			this.setLocal(index, LONG);
			this.pop();
			return;
		case FSTORE:
			this.setLocal(index, FLOAT);
			this.pop();
			return;
		case DSTORE:
			this.setLocal(index, DOUBLE);
			this.pop();
			return;
		case ASTORE:
			if (index >= this.localCount)
			{
				this.setLocal(index, this.peek());
			}
			this.pop();
			return;
		}
	}
	
	public void visitFieldInsn(int opcode, Object returnType) throws BytecodeException
	{
		switch (opcode)
		{
		case GETSTATIC:
			this.push(returnType);
			return;
		case PUTSTATIC:
			this.pop();
			return;
		case GETFIELD:
			this.pop();
			this.push(returnType);
			return;
		case PUTFIELD:
			this.pop();
			this.pop();
			return;
		}
	}
	
	public void visitInvokeInsn(int args, Object returnType) throws BytecodeException
	{
		this.stackCount -= args;
		
		if (this.stackCount < 0)
		{
			throw new StackUnderflowException();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	public void visitFrame(MethodVisitor mv)
	{
		Object[] locals = new Object[this.localCount];
		int localIndex = 0;
		for (int i = 0; i < this.localCount; i++)
		{
			Object o = this.locals[i];
			locals[localIndex++] = o;
			if (o == LONG || o == DOUBLE)
			{
				i++;
			}
		}
		
		int stack = this.stackCount;
		while (stack > 0 && this.stack[stack - 1] == TOP)
		{
			stack--;
		}
		
		mv.visitFrame(dyvil.tools.asm.Opcodes.F_NEW, localIndex, locals, stack, this.stack);
	}
	
	public Frame copy()
	{
		Frame copy = new Frame(this.stackCount, this.stack, this.localCount, this.locals);
		copy.maxLocals = this.maxLocals;
		copy.maxStack = this.maxStack;
		copy.actualStackCount = this.actualStackCount;
		return copy;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("Frame {");
		builder.append("maxs: (").append(this.maxStack).append(", ").append(this.maxLocals).append(")");
		if (this.stackCount > 0)
		{
			builder.append(", stack: [");
			builder.append(frameTypeName(this.stack[0]));
			for (int i = 1; i < this.stackCount; i++)
			{
				builder.append(", ").append(frameTypeName(this.stack[i]));
			}
			builder.append(']');
		}
		if (this.localCount > 0)
		{
			builder.append(", locals: [");
			builder.append(frameTypeName(this.locals[0]));
			for (int i = 1; i < this.localCount; i++)
			{
				builder.append(", ").append(frameTypeName(this.locals[i]));
			}
			builder.append(']');
		}
		builder.append('}');
		return builder.toString();
	}
}
