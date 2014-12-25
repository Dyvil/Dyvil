package dyvil.tools.compiler.bytecode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.OpcodeUtil;

public class MethodWriter extends MethodVisitor
{
	private boolean	hasReturn;
	
	private List	locals		= new ArrayList();
	private Stack	typeStack	= new Stack();
	
	public MethodWriter(int mode, MethodVisitor mv)
	{
		super(mode, mv);
	}
	
	public void setConstructor(Type type)
	{
		this.locals.add(UNINITIALIZED_THIS);
		this.typeStack.add(type.getFrameType());
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
		this.typeStack.push(INTEGER);
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
		this.typeStack.push(LONG);
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
		this.typeStack.push(FLOAT);
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
		this.typeStack.push(DOUBLE);
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
		this.typeStack.push("Ljava/lang/String;");
		this.mv.visitLdcInsn(value);
	}
	
	@Override
	@Deprecated
	public void visitLdcInsn(Object obj)
	{
		Class c = obj.getClass();
		if (c == String.class)
		{
			this.typeStack.push("Ljava/lang/String;");
		}
		else if (c == Integer.class)
		{
			this.typeStack.push(INTEGER);
		}
		else if (c == Long.class)
		{
			this.typeStack.push(LONG);
		}
		else if (c == Float.class)
		{
			this.typeStack.push(FLOAT);
		}
		else if (c == Double.class)
		{
			this.typeStack.push(DOUBLE);
		}
		this.mv.visitLdcInsn(obj);
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
		this.mv.visitJumpInsn(opcode, label);
		this.mv.visitFrame(F_NEW, this.locals.size(), this.locals.toArray(), this.typeStack.size(), this.typeStack.toArray());
	}
	
	@Override
	public void visitLabel(Label label)
	{
		this.mv.visitLabel(label);
		this.mv.visitFrame(F_SAME, this.locals.size(), this.locals.toArray(), this.typeStack.size(), this.typeStack.toArray());
	}
	
	@Override
	public void visitInsn(int op)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(op);
		if (this.hasReturn)
		{
			this.typeStack.clear();
		}
		else if (op == ACONST_NULL)
		{
			this.typeStack.push(NULL);
		}
		this.mv.visitInsn(op);
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
	
	public void visitEnd(Type type)
	{
		if (!this.hasReturn)
		{
			this.mv.visitInsn(type.getReturnOpcode());
		}
		this.mv.visitMaxs(this.typeStack.capacity(), this.locals.size());
		this.mv.visitEnd();
	}
}
