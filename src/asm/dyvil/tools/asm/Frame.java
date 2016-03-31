/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2011 INRIA, France Telecom All
 * rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dyvil.tools.asm;

final class Frame
{
	static final         int DIM                   = 0xF0000000;
	static final         int ARRAY_OF              = 0x10000000;
	static final         int ELEMENT_OF            = 0xF0000000;
	static final         int KIND                  = 0xF000000;
	static final         int TOP_IF_LONG_OR_DOUBLE = 0x800000;
	static final         int VALUE                 = 0x7FFFFF;
	static final         int BASE_KIND             = 0xFF00000;
	static final         int BASE_VALUE            = 0xFFFFF;
	static final         int BASE                  = 0x1000000;
	static final         int OBJECT                = BASE | 0x700000;
	static final         int UNINITIALIZED         = BASE | 0x800000;
	private static final int LOCAL                 = 0x2000000;
	private static final int STACK                 = 0x3000000;
	static final         int TOP                   = BASE | 0;
	static final         int BOOLEAN               = BASE | 9;
	static final         int BYTE                  = BASE | 10;
	static final         int CHAR                  = BASE | 11;
	static final         int SHORT                 = BASE | 12;
	static final         int INTEGER               = BASE | 1;
	static final         int FLOAT                 = BASE | 2;
	static final         int DOUBLE                = BASE | 3;
	static final         int LONG                  = BASE | 4;
	static final         int NULL                  = BASE | 5;
	static final         int UNINITIALIZED_THIS    = BASE | 6;
	static final int[] SIZE;
	
	static
	{
		int i;
		int[] b = new int[202];
		String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDD"
				+ "CDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCD"
				+ "CDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFED" + "DDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
		for (i = 0; i < b.length; ++i)
		{
			b[i] = s.charAt(i) - 'E';
		}
		SIZE = b;
	}
	
	Label owner;
	int[] inputLocals;
	int[] inputStack;
	private int[] outputLocals;
	private int[] outputStack;
	private int   outputStackTop;
	private int   initializationCount;
	private int[] initializations;
	
	private int get(final int local)
	{
		if (this.outputLocals == null || local >= this.outputLocals.length)
		{
			// this local has never been assigned in this basic block,
			// so it is still equal to its value in the input frame
			return LOCAL | local;
		}
		int type = this.outputLocals[local];
		if (type == 0)
		{
			// this local has never been assigned in this basic block,
			// so it is still equal to its value in the input frame
			type = this.outputLocals[local] = LOCAL | local;
		}
		return type;
	}
	
	private void set(final int local, final int type)
	{
		// creates and/or resizes the output local variables array if necessary
		if (this.outputLocals == null)
		{
			this.outputLocals = new int[10];
		}
		int n = this.outputLocals.length;
		if (local >= n)
		{
			int[] t = new int[Math.max(local + 1, 2 * n)];
			System.arraycopy(this.outputLocals, 0, t, 0, n);
			this.outputLocals = t;
		}
		// sets the local variable
		this.outputLocals[local] = type;
	}
	
	private void push(final int type)
	{
		// creates and/or resizes the output stack array if necessary
		if (this.outputStack == null)
		{
			this.outputStack = new int[10];
		}
		int n = this.outputStack.length;
		if (this.outputStackTop >= n)
		{
			int[] t = new int[Math.max(this.outputStackTop + 1, 2 * n)];
			System.arraycopy(this.outputStack, 0, t, 0, n);
			this.outputStack = t;
		}
		// pushes the type on the output stack
		this.outputStack[this.outputStackTop++] = type;
		// updates the maximun height reached by the output stack, if needed
		int top = this.owner.inputStackTop + this.outputStackTop;
		if (top > this.owner.outputStackMax)
		{
			this.owner.outputStackMax = top;
		}
	}
	
	private void push(final ClassWriter cw, final String desc)
	{
		int type = type(cw, desc);
		if (type != 0)
		{
			this.push(type);
			if (type == LONG || type == DOUBLE)
			{
				this.push(TOP);
			}
		}
	}
	
	private static int type(final ClassWriter cw, final String desc)
	{
		String t;
		int index = desc.charAt(0) == '(' ? desc.indexOf(')') + 1 : 0;
		switch (desc.charAt(index))
		{
		case 'V':
			return 0;
		case 'Z':
		case 'C':
		case 'B':
		case 'S':
		case 'I':
			return INTEGER;
		case 'F':
			return FLOAT;
		case 'J':
			return LONG;
		case 'D':
			return DOUBLE;
		case 'L':
			// stores the internal name, not the descriptor!
			t = desc.substring(index + 1, desc.length() - 1);
			return OBJECT | cw.addType(t);
		// case '[':
		default:
			// extracts the dimensions and the element type
			int data;
			int dims = index + 1;
			while (desc.charAt(dims) == '[')
			{
				++dims;
			}
			switch (desc.charAt(dims))
			{
			case 'Z':
				data = BOOLEAN;
				break;
			case 'C':
				data = CHAR;
				break;
			case 'B':
				data = BYTE;
				break;
			case 'S':
				data = SHORT;
				break;
			case 'I':
				data = INTEGER;
				break;
			case 'F':
				data = FLOAT;
				break;
			case 'J':
				data = LONG;
				break;
			case 'D':
				data = DOUBLE;
				break;
			// case 'L':
			default:
				// stores the internal name, not the descriptor
				t = desc.substring(dims + 1, desc.length() - 1);
				data = OBJECT | cw.addType(t);
			}
			return dims - index << 28 | data;
		}
	}
	
	private int pop()
	{
		if (this.outputStackTop > 0)
		{
			return this.outputStack[--this.outputStackTop];
		}
		// if the output frame stack is empty, pops from the input stack
		return STACK | -(--this.owner.inputStackTop);
	}
	
	private void pop(final int elements)
	{
		if (this.outputStackTop >= elements)
		{
			this.outputStackTop -= elements;
		}
		else
		{
			// if the number of elements to be popped is greater than the number
			// of elements in the output stack, clear it, and pops the remaining
			// elements from the input stack.
			this.owner.inputStackTop -= elements - this.outputStackTop;
			this.outputStackTop = 0;
		}
	}
	
	private void pop(final String desc)
	{
		char c = desc.charAt(0);
		if (c == '(')
		{
			this.pop((Type.getArgumentsAndReturnSizes(desc) >> 2) - 1);
		}
		else if (c == 'J' || c == 'D')
		{
			this.pop(2);
		}
		else
		{
			this.pop(1);
		}
	}
	
	private void init(final int var)
	{
		// creates and/or resizes the initializations array if necessary
		if (this.initializations == null)
		{
			this.initializations = new int[2];
		}
		int n = this.initializations.length;
		if (this.initializationCount >= n)
		{
			int[] t = new int[Math.max(this.initializationCount + 1, 2 * n)];
			System.arraycopy(this.initializations, 0, t, 0, n);
			this.initializations = t;
		}
		// stores the type to be initialized
		this.initializations[this.initializationCount++] = var;
	}
	
	private int init(final ClassWriter cw, final int t)
	{
		int s;
		if (t == UNINITIALIZED_THIS)
		{
			s = OBJECT | cw.addType(cw.thisName);
		}
		else if ((t & (DIM | BASE_KIND)) == UNINITIALIZED)
		{
			String type = cw.typeTable[t & BASE_VALUE].strVal1;
			s = OBJECT | cw.addType(type);
		}
		else
		{
			return t;
		}
		for (int j = 0; j < this.initializationCount; ++j)
		{
			int u = this.initializations[j];
			int dim = u & DIM;
			int kind = u & KIND;
			if (kind == LOCAL)
			{
				u = dim + this.inputLocals[u & VALUE];
			}
			else if (kind == STACK)
			{
				u = dim + this.inputStack[this.inputStack.length - (u & VALUE)];
			}
			if (t == u)
			{
				return s;
			}
		}
		return t;
	}
	
	void initInputFrame(final ClassWriter cw, final int access, final Type[] args, final int maxLocals)
	{
		this.inputLocals = new int[maxLocals];
		this.inputStack = new int[0];
		int i = 0;
		if ((access & ASMConstants.ACC_STATIC) == 0)
		{
			if ((access & MethodWriter.ACC_CONSTRUCTOR) == 0)
			{
				this.inputLocals[i++] = OBJECT | cw.addType(cw.thisName);
			}
			else
			{
				this.inputLocals[i++] = UNINITIALIZED_THIS;
			}
		}
		for (Type arg : args)
		{
			int t = type(cw, arg.getDescriptor());
			this.inputLocals[i++] = t;
			if (t == LONG || t == DOUBLE)
			{
				this.inputLocals[i++] = TOP;
			}
		}
		while (i < maxLocals)
		{
			this.inputLocals[i++] = TOP;
		}
	}
	
	void execute(final int opcode, final int arg, final ClassWriter cw, final Item item)
	{
		int t1, t2, t3, t4;
		switch (opcode)
		{
		case ASMConstants.NOP:
		case ASMConstants.INEG:
		case ASMConstants.LNEG:
		case ASMConstants.FNEG:
		case ASMConstants.DNEG:
		case ASMConstants.I2B:
		case ASMConstants.I2C:
		case ASMConstants.I2S:
		case ASMConstants.GOTO:
		case ASMConstants.RETURN:
			break;
		case ASMConstants.ACONST_NULL:
			this.push(NULL);
			break;
		case ASMConstants.ICONST_M1:
		case ASMConstants.ICONST_0:
		case ASMConstants.ICONST_1:
		case ASMConstants.ICONST_2:
		case ASMConstants.ICONST_3:
		case ASMConstants.ICONST_4:
		case ASMConstants.ICONST_5:
		case ASMConstants.BIPUSH:
		case ASMConstants.SIPUSH:
		case ASMConstants.ILOAD:
			this.push(INTEGER);
			break;
		case ASMConstants.LCONST_0:
		case ASMConstants.LCONST_1:
		case ASMConstants.LLOAD:
			this.push(LONG);
			this.push(TOP);
			break;
		case ASMConstants.FCONST_0:
		case ASMConstants.FCONST_1:
		case ASMConstants.FCONST_2:
		case ASMConstants.FLOAD:
			this.push(FLOAT);
			break;
		case ASMConstants.DCONST_0:
		case ASMConstants.DCONST_1:
		case ASMConstants.DLOAD:
			this.push(DOUBLE);
			this.push(TOP);
			break;
		case ASMConstants.LDC:
			switch (item.type)
			{
			case ClassWriter.INT:
				this.push(INTEGER);
				break;
			case ClassWriter.LONG:
				this.push(LONG);
				this.push(TOP);
				break;
			case ClassWriter.FLOAT:
				this.push(FLOAT);
				break;
			case ClassWriter.DOUBLE:
				this.push(DOUBLE);
				this.push(TOP);
				break;
			case ClassWriter.CLASS:
				this.push(OBJECT | cw.addType("java/lang/Class"));
				break;
			case ClassWriter.STR:
				this.push(OBJECT | cw.addType("java/lang/String"));
				break;
			case ClassWriter.MTYPE:
				this.push(OBJECT | cw.addType("java/lang/invoke/MethodType"));
				break;
			// case ClassWriter.HANDLE_BASE + [1..9]:
			default:
				this.push(OBJECT | cw.addType("java/lang/invoke/MethodHandle"));
			}
			break;
		case ASMConstants.ALOAD:
			this.push(this.get(arg));
			break;
		case ASMConstants.IALOAD:
		case ASMConstants.BALOAD:
		case ASMConstants.CALOAD:
		case ASMConstants.SALOAD:
			this.pop(2);
			this.push(INTEGER);
			break;
		case ASMConstants.LALOAD:
		case ASMConstants.D2L:
			this.pop(2);
			this.push(LONG);
			this.push(TOP);
			break;
		case ASMConstants.FALOAD:
			this.pop(2);
			this.push(FLOAT);
			break;
		case ASMConstants.DALOAD:
		case ASMConstants.L2D:
			this.pop(2);
			this.push(DOUBLE);
			this.push(TOP);
			break;
		case ASMConstants.AALOAD:
			this.pop(1);
			t1 = this.pop();
			this.push(ELEMENT_OF + t1);
			break;
		case ASMConstants.ISTORE:
		case ASMConstants.FSTORE:
		case ASMConstants.ASTORE:
			t1 = this.pop();
			this.set(arg, t1);
			if (arg > 0)
			{
				t2 = this.get(arg - 1);
				// if t2 is of kind STACK or LOCAL we cannot know its size!
				if (t2 == LONG || t2 == DOUBLE)
				{
					this.set(arg - 1, TOP);
				}
				else if ((t2 & KIND) != BASE)
				{
					this.set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
				}
			}
			break;
		case ASMConstants.LSTORE:
		case ASMConstants.DSTORE:
			this.pop(1);
			t1 = this.pop();
			this.set(arg, t1);
			this.set(arg + 1, TOP);
			if (arg > 0)
			{
				t2 = this.get(arg - 1);
				// if t2 is of kind STACK or LOCAL we cannot know its size!
				if (t2 == LONG || t2 == DOUBLE)
				{
					this.set(arg - 1, TOP);
				}
				else if ((t2 & KIND) != BASE)
				{
					this.set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
				}
			}
			break;
		case ASMConstants.IASTORE:
		case ASMConstants.BASTORE:
		case ASMConstants.CASTORE:
		case ASMConstants.SASTORE:
		case ASMConstants.FASTORE:
		case ASMConstants.AASTORE:
			this.pop(3);
			break;
		case ASMConstants.LASTORE:
		case ASMConstants.DASTORE:
			this.pop(4);
			break;
		case ASMConstants.POP:
		case ASMConstants.IFEQ:
		case ASMConstants.IFNE:
		case ASMConstants.IFLT:
		case ASMConstants.IFGE:
		case ASMConstants.IFGT:
		case ASMConstants.IFLE:
		case ASMConstants.IRETURN:
		case ASMConstants.FRETURN:
		case ASMConstants.ARETURN:
		case ASMConstants.TABLESWITCH:
		case ASMConstants.LOOKUPSWITCH:
		case ASMConstants.ATHROW:
		case ASMConstants.MONITORENTER:
		case ASMConstants.MONITOREXIT:
		case ASMConstants.IFNULL:
		case ASMConstants.IFNONNULL:
			this.pop(1);
			break;
		case ASMConstants.POP2:
		case ASMConstants.IF_ICMPEQ:
		case ASMConstants.IF_ICMPNE:
		case ASMConstants.IF_ICMPLT:
		case ASMConstants.IF_ICMPGE:
		case ASMConstants.IF_ICMPGT:
		case ASMConstants.IF_ICMPLE:
		case ASMConstants.IF_ACMPEQ:
		case ASMConstants.IF_ACMPNE:
		case ASMConstants.LRETURN:
		case ASMConstants.DRETURN:
			this.pop(2);
			break;
		case ASMConstants.DUP:
			t1 = this.pop();
			this.push(t1);
			this.push(t1);
			break;
		case ASMConstants.DUP_X1:
			t1 = this.pop();
			t2 = this.pop();
			this.push(t1);
			this.push(t2);
			this.push(t1);
			break;
		case ASMConstants.DUP_X2:
			t1 = this.pop();
			t2 = this.pop();
			t3 = this.pop();
			this.push(t1);
			this.push(t3);
			this.push(t2);
			this.push(t1);
			break;
		case ASMConstants.DUP2:
			t1 = this.pop();
			t2 = this.pop();
			this.push(t2);
			this.push(t1);
			this.push(t2);
			this.push(t1);
			break;
		case ASMConstants.DUP2_X1:
			t1 = this.pop();
			t2 = this.pop();
			t3 = this.pop();
			this.push(t2);
			this.push(t1);
			this.push(t3);
			this.push(t2);
			this.push(t1);
			break;
		case ASMConstants.DUP2_X2:
			t1 = this.pop();
			t2 = this.pop();
			t3 = this.pop();
			t4 = this.pop();
			this.push(t2);
			this.push(t1);
			this.push(t4);
			this.push(t3);
			this.push(t2);
			this.push(t1);
			break;
		case ASMConstants.SWAP:
			t1 = this.pop();
			t2 = this.pop();
			this.push(t1);
			this.push(t2);
			break;
		case ASMConstants.IADD:
		case ASMConstants.ISUB:
		case ASMConstants.IMUL:
		case ASMConstants.IDIV:
		case ASMConstants.IREM:
		case ASMConstants.IAND:
		case ASMConstants.IOR:
		case ASMConstants.IXOR:
		case ASMConstants.ISHL:
		case ASMConstants.ISHR:
		case ASMConstants.IUSHR:
		case ASMConstants.L2I:
		case ASMConstants.D2I:
		case ASMConstants.FCMPL:
		case ASMConstants.FCMPG:
			this.pop(2);
			this.push(INTEGER);
			break;
		case ASMConstants.LADD:
		case ASMConstants.LSUB:
		case ASMConstants.LMUL:
		case ASMConstants.LDIV:
		case ASMConstants.LREM:
		case ASMConstants.LAND:
		case ASMConstants.LOR:
		case ASMConstants.LXOR:
			this.pop(4);
			this.push(LONG);
			this.push(TOP);
			break;
		case ASMConstants.FADD:
		case ASMConstants.FSUB:
		case ASMConstants.FMUL:
		case ASMConstants.FDIV:
		case ASMConstants.FREM:
		case ASMConstants.L2F:
		case ASMConstants.D2F:
			this.pop(2);
			this.push(FLOAT);
			break;
		case ASMConstants.DADD:
		case ASMConstants.DSUB:
		case ASMConstants.DMUL:
		case ASMConstants.DDIV:
		case ASMConstants.DREM:
			this.pop(4);
			this.push(DOUBLE);
			this.push(TOP);
			break;
		case ASMConstants.LSHL:
		case ASMConstants.LSHR:
		case ASMConstants.LUSHR:
			this.pop(3);
			this.push(LONG);
			this.push(TOP);
			break;
		case ASMConstants.IINC:
			this.set(arg, INTEGER);
			break;
		case ASMConstants.I2L:
		case ASMConstants.F2L:
			this.pop(1);
			this.push(LONG);
			this.push(TOP);
			break;
		case ASMConstants.I2F:
			this.pop(1);
			this.push(FLOAT);
			break;
		case ASMConstants.I2D:
		case ASMConstants.F2D:
			this.pop(1);
			this.push(DOUBLE);
			this.push(TOP);
			break;
		case ASMConstants.F2I:
		case ASMConstants.ARRAYLENGTH:
		case ASMConstants.INSTANCEOF:
			this.pop(1);
			this.push(INTEGER);
			break;
		case ASMConstants.LCMP:
		case ASMConstants.DCMPL:
		case ASMConstants.DCMPG:
			this.pop(4);
			this.push(INTEGER);
			break;
		case ASMConstants.JSR:
		case ASMConstants.RET:
			throw new RuntimeException("JSR/RET are not supported with computeFrames option");
		case ASMConstants.GETSTATIC:
			this.push(cw, item.strVal3);
			break;
		case ASMConstants.PUTSTATIC:
			this.pop(item.strVal3);
			break;
		case ASMConstants.GETFIELD:
			this.pop(1);
			this.push(cw, item.strVal3);
			break;
		case ASMConstants.PUTFIELD:
			this.pop(item.strVal3);
			this.pop();
			break;
		case ASMConstants.INVOKEVIRTUAL:
		case ASMConstants.INVOKESPECIAL:
		case ASMConstants.INVOKESTATIC:
		case ASMConstants.INVOKEINTERFACE:
			this.pop(item.strVal3);
			if (opcode != ASMConstants.INVOKESTATIC)
			{
				t1 = this.pop();
				if (opcode == ASMConstants.INVOKESPECIAL && item.strVal2.charAt(0) == '<')
				{
					this.init(t1);
				}
			}
			this.push(cw, item.strVal3);
			break;
		case ASMConstants.INVOKEDYNAMIC:
			this.pop(item.strVal2);
			this.push(cw, item.strVal2);
			break;
		case ASMConstants.NEW:
			this.push(UNINITIALIZED | cw.addUninitializedType(item.strVal1, arg));
			break;
		case ASMConstants.NEWARRAY:
			this.pop();
			switch (arg)
			{
			case ASMConstants.T_BOOLEAN:
				this.push(ARRAY_OF | BOOLEAN);
				break;
			case ASMConstants.T_CHAR:
				this.push(ARRAY_OF | CHAR);
				break;
			case ASMConstants.T_BYTE:
				this.push(ARRAY_OF | BYTE);
				break;
			case ASMConstants.T_SHORT:
				this.push(ARRAY_OF | SHORT);
				break;
			case ASMConstants.T_INT:
				this.push(ARRAY_OF | INTEGER);
				break;
			case ASMConstants.T_FLOAT:
				this.push(ARRAY_OF | FLOAT);
				break;
			case ASMConstants.T_DOUBLE:
				this.push(ARRAY_OF | DOUBLE);
				break;
			// case Opcodes.T_LONG:
			default:
				this.push(ARRAY_OF | LONG);
				break;
			}
			break;
		case ASMConstants.ANEWARRAY:
			String s = item.strVal1;
			this.pop();
			if (s.charAt(0) == '[')
			{
				this.push(cw, '[' + s);
			}
			else
			{
				this.push(ARRAY_OF | OBJECT | cw.addType(s));
			}
			break;
		case ASMConstants.CHECKCAST:
			s = item.strVal1;
			this.pop();
			if (s.charAt(0) == '[')
			{
				this.push(cw, s);
			}
			else
			{
				this.push(OBJECT | cw.addType(s));
			}
			break;
		// case Opcodes.MULTIANEWARRAY:
		default:
			this.pop(arg);
			this.push(cw, item.strVal1);
			break;
		}
	}
	
	boolean merge(final ClassWriter cw, final Frame frame, final int edge)
	{
		boolean changed = false;
		int i, s, dim, kind, t;
		
		int nLocal = this.inputLocals.length;
		int nStack = this.inputStack.length;
		if (frame.inputLocals == null)
		{
			frame.inputLocals = new int[nLocal];
			changed = true;
		}
		
		for (i = 0; i < nLocal; ++i)
		{
			if (this.outputLocals != null && i < this.outputLocals.length)
			{
				s = this.outputLocals[i];
				if (s == 0)
				{
					t = this.inputLocals[i];
				}
				else
				{
					dim = s & DIM;
					kind = s & KIND;
					if (kind == BASE)
					{
						t = s;
					}
					else
					{
						if (kind == LOCAL)
						{
							t = dim + this.inputLocals[s & VALUE];
						}
						else
						{
							t = dim + this.inputStack[nStack - (s & VALUE)];
						}
						if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE))
						{
							t = TOP;
						}
					}
				}
			}
			else
			{
				t = this.inputLocals[i];
			}
			if (this.initializations != null)
			{
				t = this.init(cw, t);
			}
			changed |= merge(cw, t, frame.inputLocals, i);
		}
		
		if (edge > 0)
		{
			for (i = 0; i < nLocal; ++i)
			{
				t = this.inputLocals[i];
				changed |= merge(cw, t, frame.inputLocals, i);
			}
			if (frame.inputStack == null)
			{
				frame.inputStack = new int[1];
				changed = true;
			}
			changed |= merge(cw, edge, frame.inputStack, 0);
			return changed;
		}
		
		int nInputStack = this.inputStack.length + this.owner.inputStackTop;
		if (frame.inputStack == null)
		{
			frame.inputStack = new int[nInputStack + this.outputStackTop];
			changed = true;
		}
		
		for (i = 0; i < nInputStack; ++i)
		{
			t = this.inputStack[i];
			if (this.initializations != null)
			{
				t = this.init(cw, t);
			}
			changed |= merge(cw, t, frame.inputStack, i);
		}
		for (i = 0; i < this.outputStackTop; ++i)
		{
			s = this.outputStack[i];
			dim = s & DIM;
			kind = s & KIND;
			if (kind == BASE)
			{
				t = s;
			}
			else
			{
				if (kind == LOCAL)
				{
					t = dim + this.inputLocals[s & VALUE];
				}
				else
				{
					t = dim + this.inputStack[nStack - (s & VALUE)];
				}
				if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE))
				{
					t = TOP;
				}
			}
			if (this.initializations != null)
			{
				t = this.init(cw, t);
			}
			changed |= merge(cw, t, frame.inputStack, nInputStack + i);
		}
		return changed;
	}
	
	private static boolean merge(final ClassWriter cw, int t, final int[] types, final int index)
	{
		int u = types[index];
		if (u == t)
		{
			// if the types are equal, merge(u,t)=u, so there is no change
			return false;
		}
		if ((t & ~DIM) == NULL)
		{
			if (u == NULL)
			{
				return false;
			}
			t = NULL;
		}
		if (u == 0)
		{
			// if types[index] has never been assigned, merge(u,t)=t
			types[index] = t;
			return true;
		}
		int v;
		if ((u & BASE_KIND) == OBJECT || (u & DIM) != 0)
		{
			// if u is a reference type of any dimension
			if (t == NULL)
			{
				// if t is the NULL type, merge(u,t)=u, so there is no change
				return false;
			}
			else if ((t & (DIM | BASE_KIND)) == (u & (DIM | BASE_KIND)))
			{
				// if t and u have the same dimension and same base kind
				if ((u & BASE_KIND) == OBJECT)
				{
					// if t is also a reference type, and if u and t have the
					// same dimension merge(u,t) = dim(t) | common parent of the
					// element types of u and t
					v = t & DIM | OBJECT | cw.getMergedType(t & BASE_VALUE, u & BASE_VALUE);
				}
				else
				{
					// if u and t are array types, but not with the same element
					// type, merge(u,t) = dim(u) - 1 | java/lang/Object
					int vdim = ELEMENT_OF + (u & DIM);
					v = vdim | OBJECT | cw.addType("java/lang/Object");
				}
			}
			else if ((t & BASE_KIND) == OBJECT || (t & DIM) != 0)
			{
				// if t is any other reference or array type, the merged type
				// is min(udim, tdim) | java/lang/Object, where udim is the
				// array dimension of u, minus 1 if u is an array type with a
				// primitive element type (and similarly for tdim).
				int tdim = ((t & DIM) == 0 || (t & BASE_KIND) == OBJECT ? 0 : ELEMENT_OF) + (t & DIM);
				int udim = ((u & DIM) == 0 || (u & BASE_KIND) == OBJECT ? 0 : ELEMENT_OF) + (u & DIM);
				v = Math.min(tdim, udim) | OBJECT | cw.addType("java/lang/Object");
			}
			else
			{
				// if t is any other type, merge(u,t)=TOP
				v = TOP;
			}
		}
		else if (u == NULL)
		{
			// if u is the NULL type, merge(u,t)=t,
			// or TOP if t is not a reference type
			v = (t & BASE_KIND) == OBJECT || (t & DIM) != 0 ? t : TOP;
		}
		else
		{
			// if u is any other type, merge(u,t)=TOP whatever t
			v = TOP;
		}
		if (u != v)
		{
			types[index] = v;
			return true;
		}
		return false;
	}
}
