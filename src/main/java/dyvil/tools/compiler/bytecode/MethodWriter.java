package dyvil.tools.compiler.bytecode;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.OpcodeUtil;

public class MethodWriter extends MethodVisitor
{
	private boolean		hasReturn;
	private int			maxStack;
	
	private List		locals		= new ArrayList();
	private Stack<Type>	typeStack	= new Stack();
	
	public MethodWriter(int mode, MethodVisitor mv)
	{
		super(mode, mv);
	}
	
	public void setConstructor(Type type)
	{
		this.locals.add(UNINITIALIZED_THIS);
		this.push(type);
	}
	
	public void push(Type type)
	{
		this.typeStack.add(type);
		int size = this.typeStack.size();
		if (size > this.maxStack)
		{
			this.maxStack = size;
		}
	}
	
	@Override
	@Deprecated
	public void visitParameter(String desc, int index)
	{
	}
	
	public void visitParameter(String name, Type type, int index)
	{
		this.locals.add(type.getFrameType());
		this.mv.visitParameter(name, index);
	}
	
	@Override
	@Deprecated
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
	}
	
	public void visitLocalVariable(String name, Type type, Label start, Label end, int index)
	{
		this.locals.add(type.getFrameType());
		this.mv.visitLocalVariable(name, type.getExtendedName(), type.getSignature(), start, end, index);
	}
	
	public void visitLdcInsn(int value)
	{
		this.push(Type.INT);
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
		this.push(Type.LONG);
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
		this.push(Type.FLOAT);
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
		this.push(Type.DOUBLE);
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
		this.push(Type.STRING);
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	@Deprecated
	public void visitLdcInsn(Object obj)
	{
		Class c = obj.getClass();
		if (c == String.class)
		{
			this.push(Type.STRING);
		}
		else if (c == Integer.class)
		{
			this.push(Type.INT);
		}
		else if (c == Long.class)
		{
			this.push(Type.LONG);
		}
		else if (c == Float.class)
		{
			this.push(Type.FLOAT);
		}
		else if (c == Double.class)
		{
			this.push(Type.DOUBLE);
		}
		this.mv.visitLdcInsn(obj);
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
		this.mv.visitJumpInsn(opcode, label);
		this.visitFrame();
	}
	
	@Override
	public void visitLabel(Label label)
	{
		this.mv.visitLabel(label);
		this.visitFrame();
	}
	
	private void visitFrame()
	{
		int len = this.typeStack.size();
		Object[] o = new Object[len];
		for (int i = 0; i < len; i++)
		{
			o[i] = this.typeStack.get(i).getFrameType();
		}
		this.mv.visitFrame(F_SAME, this.locals.size(), this.locals.toArray(), len, o);
	}
	
	@Override
	public void visitInsn(int opcode)
	{
		this.mv.visitInsn(opcode);
	}
	
	public void visitInsn(int opcode, Type type)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(opcode);
		if (this.hasReturn)
		{
			this.typeStack.clear();
		}
		if (type != null)
		{
			this.typeStack.push(type);
		}
		this.mv.visitInsn(opcode);
	}
	
	public void visitInsn(int opcode, Type type, int pop)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(opcode);
		if (this.hasReturn)
		{
			this.typeStack.clear();
		}
		
		if (type != null)
		{
			this.typeStack.push(type);
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
	
	public void visitTypeInsn(int opcode, Type type)
	{
		if ((opcode == ANEWARRAY || opcode == NEWARRAY) && type instanceof PrimitiveType)
		{
			this.mv.visitIntInsn(NEWARRAY, ((PrimitiveType) type).typecode);
			return;
		}
		this.mv.visitTypeInsn(opcode, type.getInternalName());
	}
	
	@Override
	@Deprecated
	public void visitVarInsn(int opcode, int index)
	{
		this.mv.visitVarInsn(opcode, index);
	}
	
	public void visitVarInsn(int opcode, int index, Type type)
	{
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitVarInsn(opcode, index);
	}
	
	@Override
	@Deprecated
	public void visitFieldInsn(int opcode, String owner, String name, String desc)
	{
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	public void visitFieldInsn(int opcode, String owner, String name, String desc, Type type)
	{
		if (type != null)
		{
			this.push(type);
		}
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface)
	{
		this.mv.visitMethodInsn(opcode, owner, name, desc, isInterface);
	}
	
	public void visitEnd(Type type)
	{
		if (!this.hasReturn)
		{
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(this.maxStack, this.locals.size());
		this.mv.visitEnd();
	}
}
