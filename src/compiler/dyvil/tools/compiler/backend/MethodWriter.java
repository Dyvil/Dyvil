package dyvil.tools.compiler.backend;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.util.OpcodeUtil;

public final class MethodWriter extends MethodVisitor
{
	public static final Object	JUMP_INSTRUCTION_TARGET	= new Object();
	
	private boolean				hasReturn;
	private int					maxStack;
	
	private List				locals					= new ArrayList();
	private Stack				typeStack				= new Stack();
	
	public MethodWriter(MethodVisitor mv)
	{
		super(ASM5, mv);
	}
	
	public void setConstructor(IType type)
	{
		this.locals.add(UNINITIALIZED_THIS);
		this.push(UNINITIALIZED_THIS);
	}
	
	protected void push(Object type)
	{
		this.typeStack.add(type);
		int size = this.typeStack.size();
		if (size > this.maxStack)
		{
			this.maxStack = size;
		}
	}
	
	public void push(IType type)
	{
		Object frameType = type.getFrameType();
		if (frameType != null)
		{
			this.typeStack.add(frameType);
			int size = this.typeStack.size();
			if (size > this.maxStack)
			{
				this.maxStack = size;
			}
		}
	}
	
	@Override
	@Deprecated
	public void visitParameter(String desc, int index)
	{
	}
	
	public void visitParameter(String name, IType type, int index)
	{
		this.locals.add(type.getFrameType());
		this.mv.visitParameter(name, index);
	}
	
	@Override
	@Deprecated
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
	}
	
	public void visitLocalVariable(String name, IType type, Label start, Label end, int index)
	{
		this.locals.add(type.getFrameType());
		this.mv.visitLocalVariable(name, type.getExtendedName(), type.getSignature(), start, end, index);
	}
	
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
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
		if (opcode >= IFEQ && opcode <= IFLE)
		{
			this.typeStack.pop();
		}
		this.mv.visitJumpInsn(opcode, label);
	}
	
	@Override
	public void visitLabel(Label label)
	{
		if (label.info == JUMP_INSTRUCTION_TARGET)
		{
			this.visitFrame();
		}
		this.mv.visitLabel(label);
	}
	
	private void visitFrame()
	{
		int len = this.typeStack.size();
		Object[] o = new Object[len];
		for (int i = 0; i < len; i++)
		{
			o[i] = this.typeStack.get(i);
		}
		this.mv.visitFrame(F_NEW, this.locals.size(), this.locals.toArray(), len, o);
	}
	
	@Override
	public void visitInsn(int opcode)
	{
		if (opcode > 255)
		{
			if (opcode == Opcodes.LCONST_M1)
			{
				this.mv.visitLdcInsn(-1L);
			}
		}
		else if (opcode == POP)
		{
			this.typeStack.pop();
		}
		else if (opcode >= IALOAD && opcode <= SALOAD)
		{
			this.typeStack.pop(); // Index
			String s = (String) this.typeStack.pop(); // Array
			this.push(s.substring(1));
		}
		else if (opcode >= IASTORE && opcode <= SASTORE)
		{
			this.typeStack.pop(); // Index
			this.typeStack.pop(); // Array
			this.typeStack.pop(); // Value
		}
		this.mv.visitInsn(opcode);
	}
	
	public void visitInsn(int opcode, IType type)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(opcode);
		if (this.hasReturn)
		{
			this.typeStack.clear();
		}
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitInsn(opcode);
	}
	
	public void visitInsn(int opcode, IType type, int pop)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(opcode);
		if (this.hasReturn)
		{
			this.typeStack.clear();
		}
		
		if (type != null)
		{
			this.push(type);
		}
		while (pop > 0)
		{
			this.typeStack.pop();
			pop--;
		}
		this.mv.visitInsn(opcode);
	}
	
	@Override
	@Deprecated
	public void visitTypeInsn(int opcode, String type)
	{
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
		this.mv.visitTypeInsn(opcode, type.getInternalName());
	}
	
	@Override
	public void visitVarInsn(int opcode, int index)
	{
		this.push(this.locals.get(index));
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
			this.typeStack.pop();
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
		this.typeStack.pop(); // Value
		this.mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
	}
	
	public void visitGetField(String owner, String name, String desc, IType type)
	{
		this.typeStack.pop(); // Instance
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitFieldInsn(GETSTATIC, owner, name, desc);
	}
	
	public void visitPutField(String owner, String name, String desc)
	{
		this.typeStack.pop(); // Instance
		this.typeStack.pop(); // Value
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
			this.typeStack.pop();
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
			this.typeStack.pop();
		}
		if (returnType != null)
		{
			this.push(returnType);
		}
	}
	
	@Override
	@Deprecated
	public void visitEnd()
	{
		this.mv.visitEnd();
	}
	
	public void visitEnd(IType type)
	{
		if (!this.hasReturn)
		{
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(this.maxStack, this.locals.size());
		this.mv.visitEnd();
	}
}
