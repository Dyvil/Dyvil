package dyvil.tools.compiler.bytecode;

import java.util.*;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.OpcodeUtil;

public class MethodWriter extends MethodVisitor
{
	private boolean					hasReturn;
	
	private List					locals		= new ArrayList();
	private Stack					typeStack	= new Stack();
	
	public MethodWriter(int mode, MethodVisitor mv)
	{
		super(mode, mv);
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
	
	@Override
	public void visitLdcInsn(Object obj)
	{
		Class c = obj.getClass();
		if (c == String.class)
		{
			typeStack.push("Ljava/lang/String;");
		}
		else if (c == Integer.class)
		{
			typeStack.push(Opcodes.INTEGER);
		}
		else if (c == Long.class)
		{
			typeStack.push(Opcodes.LONG);
		}
		else if (c == Float.class)
		{
			typeStack.push(Opcodes.FLOAT);
		}
		else if (c == Double.class)
		{
			typeStack.push(Opcodes.DOUBLE);
		}
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label)
	{
		this.mv.visitJumpInsn(opcode, label);
		this.mv.visitFrame(Opcodes.F_NEW, this.locals.size(), this.locals.toArray(), this.typeStack.size(), this.typeStack.toArray());
	}
	
	@Override
	public void visitLabel(Label label)
	{
		this.mv.visitLabel(label);
		this.mv.visitFrame(Opcodes.F_SAME, this.locals.size(), this.locals.toArray(), this.typeStack.size(), this.typeStack.toArray());
	}
	
	@Override
	public void visitInsn(int op)
	{
		this.hasReturn = OpcodeUtil.isReturnOpcode(op);
		if (this.hasReturn)
		{
			typeStack.clear();
		}
		else if (op == Opcodes.ACONST_NULL)
		{
			typeStack.push(Opcodes.NULL);
		}
		this.mv.visitInsn(op);
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
