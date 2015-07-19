/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package dyvil.tools.asm.util;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import dyvil.tools.asm.*;

/**
 * A {@link Printer} that prints the ASM code to generate the classes if visits.
 * 
 * @author Eric Bruneton
 */
public class ASMifier extends Printer
{
	
	/**
	 * The name of the visitor variable in the produced code.
	 */
	protected final String			name;
	
	/**
	 * Identifier of the annotation visitor variable in the produced code.
	 */
	protected final int				id;
	
	/**
	 * The label names. This map associates String values to Label keys. It is
	 * used only in ASMifierMethodVisitor.
	 */
	protected Map<Label, String>	labelNames;
	
	/**
	 * Pseudo access flag used to distinguish class access flags.
	 */
	private static final int		ACCESS_CLASS	= 262144;
	
	/**
	 * Pseudo access flag used to distinguish field access flags.
	 */
	private static final int		ACCESS_FIELD	= 524288;
	
	/**
	 * Pseudo access flag used to distinguish inner class flags.
	 */
	private static final int		ACCESS_INNER	= 1048576;
	
	/**
	 * Constructs a new {@link ASMifier}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the
	 * {@link #ASMifier(int, String, int)} version.
	 * 
	 * @throws IllegalStateException
	 *             If a subclass calls this constructor.
	 */
	public ASMifier()
	{
		this(Opcodes.ASM5, "cw", 0);
		if (this.getClass() != ASMifier.class)
		{
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Constructs a new {@link ASMifier}.
	 * 
	 * @param api
	 *            the ASM API version implemented by this class. Must be one of
	 *            {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
	 * @param name
	 *            the name of the visitor variable in the produced code.
	 * @param id
	 *            identifier of the annotation visitor variable in the produced
	 *            code.
	 */
	protected ASMifier(final int api, final String name, final int id)
	{
		super(api);
		this.name = name;
		this.id = id;
	}
	
	/**
	 * Prints the ASM source code to generate the given class to the standard
	 * output.
	 * <p>
	 * Usage: ASMifier [-debug] &lt;binary class name or class file name&gt;
	 * 
	 * @param args
	 *            the command line arguments.
	 * @throws Exception
	 *             if the class cannot be found, or if an IO exception occurs.
	 */
	public static void main(final String[] args) throws Exception
	{
		int i = 0;
		int flags = ClassReader.SKIP_DEBUG;
		
		boolean ok = true;
		if (args.length < 1 || args.length > 2)
		{
			ok = false;
		}
		if (ok && "-debug".equals(args[0]))
		{
			i = 1;
			flags = 0;
			if (args.length != 2)
			{
				ok = false;
			}
		}
		if (!ok)
		{
			System.err.println("Prints the ASM code to generate the given class.");
			System.err.println("Usage: ASMifier [-debug] " + "<fully qualified class name or class file name>");
			return;
		}
		ClassReader cr;
		if (args[i].endsWith(".class") || args[i].indexOf('\\') > -1 || args[i].indexOf('/') > -1)
		{
			cr = new ClassReader(new FileInputStream(args[i]));
		}
		else
		{
			cr = new ClassReader(args[i]);
		}
		cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), flags);
	}
	
	// ------------------------------------------------------------------------
	// Classes
	// ------------------------------------------------------------------------
	
	@Override
	public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces)
	{
		String simpleName;
		int n = name.lastIndexOf('/');
		if (n == -1)
		{
			simpleName = name;
		}
		else
		{
			this.text.add("package asm." + name.substring(0, n).replace('/', '.') + ";\n");
			simpleName = name.substring(n + 1);
		}
		this.text.add("import java.util.*;\n");
		this.text.add("import org.objectweb.asm.*;\n");
		this.text.add("public class " + simpleName + "Dump implements Opcodes {\n\n");
		this.text.add("public static byte[] dump () throws Exception {\n\n");
		this.text.add("ClassWriter cw = new ClassWriter(0);\n");
		this.text.add("FieldVisitor fv;\n");
		this.text.add("MethodVisitor mv;\n");
		this.text.add("AnnotationVisitor av0;\n\n");
		
		this.buf.setLength(0);
		this.buf.append("cw.visit(");
		switch (version)
		{
		case Opcodes.V1_1:
			this.buf.append("V1_1");
			break;
		case Opcodes.V1_2:
			this.buf.append("V1_2");
			break;
		case Opcodes.V1_3:
			this.buf.append("V1_3");
			break;
		case Opcodes.V1_4:
			this.buf.append("V1_4");
			break;
		case Opcodes.V1_5:
			this.buf.append("V1_5");
			break;
		case Opcodes.V1_6:
			this.buf.append("V1_6");
			break;
		case Opcodes.V1_7:
			this.buf.append("V1_7");
			break;
		default:
			this.buf.append(version);
			break;
		}
		this.buf.append(", ");
		this.appendAccess(access | ACCESS_CLASS);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(signature);
		this.buf.append(", ");
		this.appendConstant(superName);
		this.buf.append(", ");
		if (interfaces != null && interfaces.length > 0)
		{
			this.buf.append("new String[] {");
			for (int i = 0; i < interfaces.length; ++i)
			{
				this.buf.append(i == 0 ? " " : ", ");
				this.appendConstant(interfaces[i]);
			}
			this.buf.append(" }");
		}
		else
		{
			this.buf.append("null");
		}
		this.buf.append(");\n\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitSource(final String file, final String debug)
	{
		this.buf.setLength(0);
		this.buf.append("cw.visitSource(");
		this.appendConstant(file);
		this.buf.append(", ");
		this.appendConstant(debug);
		this.buf.append(");\n\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitOuterClass(final String owner, final String name, final String desc)
	{
		this.buf.setLength(0);
		this.buf.append("cw.visitOuterClass(");
		this.appendConstant(owner);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(");\n\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public ASMifier visitClassAnnotation(final String desc, final boolean visible)
	{
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public ASMifier visitClassTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitClassAttribute(final Attribute attr)
	{
		this.visitAttribute(attr);
	}
	
	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName, final int access)
	{
		this.buf.setLength(0);
		this.buf.append("cw.visitInnerClass(");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(outerName);
		this.buf.append(", ");
		this.appendConstant(innerName);
		this.buf.append(", ");
		this.appendAccess(access | ACCESS_INNER);
		this.buf.append(");\n\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public ASMifier visitField(final int access, final String name, final String desc, final String signature, final Object value)
	{
		this.buf.setLength(0);
		this.buf.append("{\n");
		this.buf.append("fv = cw.visitField(");
		this.appendAccess(access | ACCESS_FIELD);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(", ");
		this.appendConstant(signature);
		this.buf.append(", ");
		this.appendConstant(value);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("fv", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public ASMifier visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions)
	{
		this.buf.setLength(0);
		this.buf.append("{\n");
		this.buf.append("mv = cw.visitMethod(");
		this.appendAccess(access);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(", ");
		this.appendConstant(signature);
		this.buf.append(", ");
		if (exceptions != null && exceptions.length > 0)
		{
			this.buf.append("new String[] {");
			for (int i = 0; i < exceptions.length; ++i)
			{
				this.buf.append(i == 0 ? " " : ", ");
				this.appendConstant(exceptions[i]);
			}
			this.buf.append(" }");
		}
		else
		{
			this.buf.append("null");
		}
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("mv", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public void visitClassEnd()
	{
		this.text.add("cw.visitEnd();\n\n");
		this.text.add("return cw.toByteArray();\n");
		this.text.add("}\n");
		this.text.add("}\n");
	}
	
	// ------------------------------------------------------------------------
	// Annotations
	// ------------------------------------------------------------------------
	
	@Override
	public void visit(final String name, final Object value)
	{
		this.buf.setLength(0);
		this.buf.append("av").append(this.id).append(".visit(");
		appendConstant(this.buf, name);
		this.buf.append(", ");
		appendConstant(this.buf, value);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitEnum(final String name, final String desc, final String value)
	{
		this.buf.setLength(0);
		this.buf.append("av").append(this.id).append(".visitEnum(");
		appendConstant(this.buf, name);
		this.buf.append(", ");
		appendConstant(this.buf, desc);
		this.buf.append(", ");
		appendConstant(this.buf, value);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public ASMifier visitAnnotation(final String name, final String desc)
	{
		this.buf.setLength(0);
		this.buf.append("{\n");
		this.buf.append("AnnotationVisitor av").append(this.id + 1).append(" = av");
		this.buf.append(this.id).append(".visitAnnotation(");
		appendConstant(this.buf, name);
		this.buf.append(", ");
		appendConstant(this.buf, desc);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", this.id + 1);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public ASMifier visitArray(final String name)
	{
		this.buf.setLength(0);
		this.buf.append("{\n");
		this.buf.append("AnnotationVisitor av").append(this.id + 1).append(" = av");
		this.buf.append(this.id).append(".visitArray(");
		appendConstant(this.buf, name);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", this.id + 1);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public void visitAnnotationEnd()
	{
		this.buf.setLength(0);
		this.buf.append("av").append(this.id).append(".visitEnd();\n");
		this.text.add(this.buf.toString());
	}
	
	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------
	
	@Override
	public ASMifier visitFieldAnnotation(final String desc, final boolean visible)
	{
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public ASMifier visitFieldTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitFieldAttribute(final Attribute attr)
	{
		this.visitAttribute(attr);
	}
	
	@Override
	public void visitFieldEnd()
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitEnd();\n");
		this.text.add(this.buf.toString());
	}
	
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------
	
	@Override
	public void visitParameter(String parameterName, int access)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitParameter(");
		appendString(this.buf, parameterName);
		this.buf.append(", ");
		this.appendAccess(access);
		this.text.add(this.buf.append(");\n").toString());
	}
	
	@Override
	public ASMifier visitAnnotationDefault()
	{
		this.buf.setLength(0);
		this.buf.append("{\n").append("av0 = ").append(this.name).append(".visitAnnotationDefault();\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public ASMifier visitMethodAnnotation(final String desc, final boolean visible)
	{
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public ASMifier visitMethodTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public ASMifier visitParameterAnnotation(final int parameter, final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append("{\n").append("av0 = ").append(this.name).append(".visitParameterAnnotation(").append(parameter).append(", ");
		this.appendConstant(desc);
		this.buf.append(", ").append(visible).append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public void visitMethodAttribute(final Attribute attr)
	{
		this.visitAttribute(attr);
	}
	
	@Override
	public void visitCode()
	{
		this.text.add(this.name + ".visitCode();\n");
	}
	
	@Override
	public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack)
	{
		this.buf.setLength(0);
		switch (type)
		{
		case Opcodes.F_NEW:
		case Opcodes.F_FULL:
			this.declareFrameTypes(nLocal, local);
			this.declareFrameTypes(nStack, stack);
			if (type == Opcodes.F_NEW)
			{
				this.buf.append(this.name).append(".visitFrame(Opcodes.F_NEW, ");
			}
			else
			{
				this.buf.append(this.name).append(".visitFrame(Opcodes.F_FULL, ");
			}
			this.buf.append(nLocal).append(", new Object[] {");
			this.appendFrameTypes(nLocal, local);
			this.buf.append("}, ").append(nStack).append(", new Object[] {");
			this.appendFrameTypes(nStack, stack);
			this.buf.append('}');
			break;
		case Opcodes.F_APPEND:
			this.declareFrameTypes(nLocal, local);
			this.buf.append(this.name).append(".visitFrame(Opcodes.F_APPEND,").append(nLocal).append(", new Object[] {");
			this.appendFrameTypes(nLocal, local);
			this.buf.append("}, 0, null");
			break;
		case Opcodes.F_CHOP:
			this.buf.append(this.name).append(".visitFrame(Opcodes.F_CHOP,").append(nLocal).append(", null, 0, null");
			break;
		case Opcodes.F_SAME:
			this.buf.append(this.name).append(".visitFrame(Opcodes.F_SAME, 0, null, 0, null");
			break;
		case Opcodes.F_SAME1:
			this.declareFrameTypes(1, stack);
			this.buf.append(this.name).append(".visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {");
			this.appendFrameTypes(1, stack);
			this.buf.append('}');
			break;
		}
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitInsn(final int opcode)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitInsn(").append(OPCODES[opcode]).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitIntInsn(final int opcode, final int operand)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitIntInsn(").append(OPCODES[opcode]).append(", ")
				.append(opcode == Opcodes.NEWARRAY ? TYPES[operand] : Integer.toString(operand)).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitVarInsn(final int opcode, final int var)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitVarInsn(").append(OPCODES[opcode]).append(", ").append(var).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitTypeInsn(final int opcode, final String type)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitTypeInsn(").append(OPCODES[opcode]).append(", ");
		this.appendConstant(type);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitFieldInsn(").append(OPCODES[opcode]).append(", ");
		this.appendConstant(owner);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Deprecated
	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc)
	{
		if (this.api >= Opcodes.ASM5)
		{
			super.visitMethodInsn(opcode, owner, name, desc);
			return;
		}
		this.doVisitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
	}
	
	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf)
	{
		if (this.api < Opcodes.ASM5)
		{
			super.visitMethodInsn(opcode, owner, name, desc, itf);
			return;
		}
		this.doVisitMethodInsn(opcode, owner, name, desc, itf);
	}
	
	private void doVisitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitMethodInsn(").append(OPCODES[opcode]).append(", ");
		this.appendConstant(owner);
		this.buf.append(", ");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(", ");
		this.buf.append(itf ? "true" : "false");
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitInvokeDynamicInsn(");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(", ");
		this.appendConstant(bsm);
		this.buf.append(", new Object[]{");
		for (int i = 0; i < bsmArgs.length; ++i)
		{
			this.appendConstant(bsmArgs[i]);
			if (i != bsmArgs.length - 1)
			{
				this.buf.append(", ");
			}
		}
		this.buf.append("});\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitJumpInsn(final int opcode, final Label label)
	{
		this.buf.setLength(0);
		this.declareLabel(label);
		this.buf.append(this.name).append(".visitJumpInsn(").append(OPCODES[opcode]).append(", ");
		this.appendLabel(label);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLabel(final Label label)
	{
		this.buf.setLength(0);
		this.declareLabel(label);
		this.buf.append(this.name).append(".visitLabel(");
		this.appendLabel(label);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLdcInsn(final Object cst)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitLdcInsn(");
		this.appendConstant(cst);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitIincInsn(final int var, final int increment)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitIincInsn(").append(var).append(", ").append(increment).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels)
	{
		this.buf.setLength(0);
		for (Label label : labels)
		{
			this.declareLabel(label);
		}
		this.declareLabel(dflt);
		
		this.buf.append(this.name).append(".visitTableSwitchInsn(").append(min).append(", ").append(max).append(", ");
		this.appendLabel(dflt);
		this.buf.append(", new Label[] {");
		for (int i = 0; i < labels.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ");
			this.appendLabel(labels[i]);
		}
		this.buf.append(" });\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels)
	{
		this.buf.setLength(0);
		for (Label label : labels)
		{
			this.declareLabel(label);
		}
		this.declareLabel(dflt);
		
		this.buf.append(this.name).append(".visitLookupSwitchInsn(");
		this.appendLabel(dflt);
		this.buf.append(", new int[] {");
		for (int i = 0; i < keys.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ").append(keys[i]);
		}
		this.buf.append(" }, new Label[] {");
		for (int i = 0; i < labels.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ");
			this.appendLabel(labels[i]);
		}
		this.buf.append(" });\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitMultiANewArrayInsn(");
		this.appendConstant(desc);
		this.buf.append(", ").append(dims).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public ASMifier visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation("visitInsnAnnotation", typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type)
	{
		this.buf.setLength(0);
		this.declareLabel(start);
		this.declareLabel(end);
		this.declareLabel(handler);
		this.buf.append(this.name).append(".visitTryCatchBlock(");
		this.appendLabel(start);
		this.buf.append(", ");
		this.appendLabel(end);
		this.buf.append(", ");
		this.appendLabel(handler);
		this.buf.append(", ");
		this.appendConstant(type);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public ASMifier visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation("visitTryCatchAnnotation", typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitLocalVariable(");
		this.appendConstant(name);
		this.buf.append(", ");
		this.appendConstant(desc);
		this.buf.append(", ");
		this.appendConstant(signature);
		this.buf.append(", ");
		this.appendLabel(start);
		this.buf.append(", ");
		this.appendLabel(end);
		this.buf.append(", ").append(index).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Printer visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append("{\n").append("av0 = ").append(this.name).append(".visitLocalVariableAnnotation(");
		this.buf.append(typeRef);
		this.buf.append(", TypePath.fromString(\"").append(typePath).append("\"), ");
		this.buf.append("new Label[] {");
		for (int i = 0; i < start.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ");
			this.appendLabel(start[i]);
		}
		this.buf.append(" }, new Label[] {");
		for (int i = 0; i < end.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ");
			this.appendLabel(end[i]);
		}
		this.buf.append(" }, new int[] {");
		for (int i = 0; i < index.length; ++i)
		{
			this.buf.append(i == 0 ? " " : ", ").append(index[i]);
		}
		this.buf.append(" }, ");
		this.appendConstant(desc);
		this.buf.append(", ").append(visible).append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	@Override
	public void visitLineNumber(final int line, final Label start)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitLineNumber(").append(line).append(", ");
		this.appendLabel(start);
		this.buf.append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMaxs(final int maxStack, final int maxLocals)
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitMaxs(").append(maxStack).append(", ").append(maxLocals).append(");\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMethodEnd()
	{
		this.buf.setLength(0);
		this.buf.append(this.name).append(".visitEnd();\n");
		this.text.add(this.buf.toString());
	}
	
	// ------------------------------------------------------------------------
	// Common methods
	// ------------------------------------------------------------------------
	
	public ASMifier visitAnnotation(final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append("{\n").append("av0 = ").append(this.name).append(".visitAnnotation(");
		this.appendConstant(desc);
		this.buf.append(", ").append(visible).append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	public ASMifier visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		return this.visitTypeAnnotation("visitTypeAnnotation", typeRef, typePath, desc, visible);
	}
	
	public ASMifier visitTypeAnnotation(final String method, final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append("{\n").append("av0 = ").append(this.name).append(".").append(method).append("(");
		this.buf.append(typeRef);
		this.buf.append(", TypePath.fromString(\"").append(typePath).append("\"), ");
		this.appendConstant(desc);
		this.buf.append(", ").append(visible).append(");\n");
		this.text.add(this.buf.toString());
		ASMifier a = this.createASMifier("av", 0);
		this.text.add(a.getText());
		this.text.add("}\n");
		return a;
	}
	
	public void visitAttribute(final Attribute attr)
	{
		this.buf.setLength(0);
		this.buf.append("// ATTRIBUTE ").append(attr.type).append('\n');
		if (attr instanceof ASMifiable)
		{
			if (this.labelNames == null)
			{
				this.labelNames = new HashMap<Label, String>();
			}
			this.buf.append("{\n");
			((ASMifiable) attr).asmify(this.buf, "attr", this.labelNames);
			this.buf.append(this.name).append(".visitAttribute(attr);\n");
			this.buf.append("}\n");
		}
		this.text.add(this.buf.toString());
	}
	
	// ------------------------------------------------------------------------
	// Utility methods
	// ------------------------------------------------------------------------
	
	protected ASMifier createASMifier(final String name, final int id)
	{
		return new ASMifier(Opcodes.ASM5, name, id);
	}
	
	/**
	 * Appends a string representation of the given access modifiers to
	 * {@link #buf buf}.
	 * 
	 * @param access
	 *            some access modifiers.
	 */
	void appendAccess(final int access)
	{
		boolean first = true;
		if ((access & Opcodes.ACC_PUBLIC) != 0)
		{
			this.buf.append("ACC_PUBLIC");
			first = false;
		}
		if ((access & Opcodes.ACC_PRIVATE) != 0)
		{
			this.buf.append("ACC_PRIVATE");
			first = false;
		}
		if ((access & Opcodes.ACC_PROTECTED) != 0)
		{
			this.buf.append("ACC_PROTECTED");
			first = false;
		}
		if ((access & Opcodes.ACC_FINAL) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_FINAL");
			first = false;
		}
		if ((access & Opcodes.ACC_STATIC) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_STATIC");
			first = false;
		}
		if ((access & Opcodes.ACC_SYNCHRONIZED) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			if ((access & ACCESS_CLASS) == 0)
			{
				this.buf.append("ACC_SYNCHRONIZED");
			}
			else
			{
				this.buf.append("ACC_SUPER");
			}
			first = false;
		}
		if ((access & Opcodes.ACC_VOLATILE) != 0 && (access & ACCESS_FIELD) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_VOLATILE");
			first = false;
		}
		if ((access & Opcodes.ACC_BRIDGE) != 0 && (access & ACCESS_CLASS) == 0 && (access & ACCESS_FIELD) == 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_BRIDGE");
			first = false;
		}
		if ((access & Opcodes.ACC_VARARGS) != 0 && (access & ACCESS_CLASS) == 0 && (access & ACCESS_FIELD) == 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_VARARGS");
			first = false;
		}
		if ((access & Opcodes.ACC_TRANSIENT) != 0 && (access & ACCESS_FIELD) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_TRANSIENT");
			first = false;
		}
		if ((access & Opcodes.ACC_NATIVE) != 0 && (access & ACCESS_CLASS) == 0 && (access & ACCESS_FIELD) == 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_NATIVE");
			first = false;
		}
		if ((access & Opcodes.ACC_ENUM) != 0 && ((access & ACCESS_CLASS) != 0 || (access & ACCESS_FIELD) != 0 || (access & ACCESS_INNER) != 0))
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_ENUM");
			first = false;
		}
		if ((access & Opcodes.ACC_ANNOTATION) != 0 && ((access & ACCESS_CLASS) != 0 || (access & ACCESS_INNER) != 0))
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_ANNOTATION");
			first = false;
		}
		if ((access & Opcodes.ACC_ABSTRACT) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_ABSTRACT");
			first = false;
		}
		if ((access & Opcodes.ACC_INTERFACE) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_INTERFACE");
			first = false;
		}
		if ((access & Opcodes.ACC_STRICT) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_STRICT");
			first = false;
		}
		if ((access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_SYNTHETIC");
			first = false;
		}
		if ((access & Opcodes.ACC_DEPRECATED) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_DEPRECATED");
			first = false;
		}
		if ((access & Opcodes.ACC_MANDATED) != 0)
		{
			if (!first)
			{
				this.buf.append(" + ");
			}
			this.buf.append("ACC_MANDATED");
			first = false;
		}
		if (first)
		{
			this.buf.append('0');
		}
	}
	
	/**
	 * Appends a string representation of the given constant to the given
	 * buffer.
	 * 
	 * @param cst
	 *            an {@link Integer}, {@link Float}, {@link Long},
	 *            {@link Double} or {@link String} object. May be <tt>null</tt>.
	 */
	protected void appendConstant(final Object cst)
	{
		appendConstant(this.buf, cst);
	}
	
	/**
	 * Appends a string representation of the given constant to the given
	 * buffer.
	 * 
	 * @param buf
	 *            a string buffer.
	 * @param cst
	 *            an {@link Integer}, {@link Float}, {@link Long},
	 *            {@link Double} or {@link String} object. May be <tt>null</tt>.
	 */
	static void appendConstant(final StringBuffer buf, final Object cst)
	{
		if (cst == null)
		{
			buf.append("null");
		}
		else if (cst instanceof String)
		{
			appendString(buf, (String) cst);
		}
		else if (cst instanceof Type)
		{
			buf.append("Type.getType(\"");
			buf.append(((Type) cst).getDescriptor());
			buf.append("\")");
		}
		else if (cst instanceof Handle)
		{
			buf.append("new Handle(");
			Handle h = (Handle) cst;
			buf.append("Opcodes.").append(HANDLE_TAG[h.getTag()]).append(", \"");
			buf.append(h.getOwner()).append("\", \"");
			buf.append(h.getName()).append("\", \"");
			buf.append(h.getDesc()).append("\")");
		}
		else if (cst instanceof Byte)
		{
			buf.append("new Byte((byte)").append(cst).append(')');
		}
		else if (cst instanceof Boolean)
		{
			buf.append(((Boolean) cst).booleanValue() ? "Boolean.TRUE" : "Boolean.FALSE");
		}
		else if (cst instanceof Short)
		{
			buf.append("new Short((short)").append(cst).append(')');
		}
		else if (cst instanceof Character)
		{
			int c = ((Character) cst).charValue();
			buf.append("new Character((char)").append(c).append(')');
		}
		else if (cst instanceof Integer)
		{
			buf.append("new Integer(").append(cst).append(')');
		}
		else if (cst instanceof Float)
		{
			buf.append("new Float(\"").append(cst).append("\")");
		}
		else if (cst instanceof Long)
		{
			buf.append("new Long(").append(cst).append("L)");
		}
		else if (cst instanceof Double)
		{
			buf.append("new Double(\"").append(cst).append("\")");
		}
		else if (cst instanceof byte[])
		{
			byte[] v = (byte[]) cst;
			buf.append("new byte[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]);
			}
			buf.append('}');
		}
		else if (cst instanceof boolean[])
		{
			boolean[] v = (boolean[]) cst;
			buf.append("new boolean[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]);
			}
			buf.append('}');
		}
		else if (cst instanceof short[])
		{
			short[] v = (short[]) cst;
			buf.append("new short[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append("(short)").append(v[i]);
			}
			buf.append('}');
		}
		else if (cst instanceof char[])
		{
			char[] v = (char[]) cst;
			buf.append("new char[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append("(char)").append((int) v[i]);
			}
			buf.append('}');
		}
		else if (cst instanceof int[])
		{
			int[] v = (int[]) cst;
			buf.append("new int[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]);
			}
			buf.append('}');
		}
		else if (cst instanceof long[])
		{
			long[] v = (long[]) cst;
			buf.append("new long[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]).append('L');
			}
			buf.append('}');
		}
		else if (cst instanceof float[])
		{
			float[] v = (float[]) cst;
			buf.append("new float[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]).append('f');
			}
			buf.append('}');
		}
		else if (cst instanceof double[])
		{
			double[] v = (double[]) cst;
			buf.append("new double[] {");
			for (int i = 0; i < v.length; i++)
			{
				buf.append(i == 0 ? "" : ",").append(v[i]).append('d');
			}
			buf.append('}');
		}
	}
	
	private void declareFrameTypes(final int n, final Object[] o)
	{
		for (int i = 0; i < n; ++i)
		{
			if (o[i] instanceof Label)
			{
				this.declareLabel((Label) o[i]);
			}
		}
	}
	
	private void appendFrameTypes(final int n, final Object[] o)
	{
		for (int i = 0; i < n; ++i)
		{
			if (i > 0)
			{
				this.buf.append(", ");
			}
			if (o[i] instanceof String)
			{
				this.appendConstant(o[i]);
			}
			else if (o[i] instanceof Integer)
			{
				switch (((Integer) o[i]).intValue())
				{
				case 0:
					this.buf.append("Opcodes.TOP");
					break;
				case 1:
					this.buf.append("Opcodes.INTEGER");
					break;
				case 2:
					this.buf.append("Opcodes.FLOAT");
					break;
				case 3:
					this.buf.append("Opcodes.DOUBLE");
					break;
				case 4:
					this.buf.append("Opcodes.LONG");
					break;
				case 5:
					this.buf.append("Opcodes.NULL");
					break;
				case 6:
					this.buf.append("Opcodes.UNINITIALIZED_THIS");
					break;
				}
			}
			else
			{
				this.appendLabel((Label) o[i]);
			}
		}
	}
	
	/**
	 * Appends a declaration of the given label to {@link #buf buf}. This
	 * declaration is of the form "Label lXXX = new Label();". Does nothing if
	 * the given label has already been declared.
	 * 
	 * @param l
	 *            a label.
	 */
	protected void declareLabel(final Label l)
	{
		if (this.labelNames == null)
		{
			this.labelNames = new HashMap<Label, String>();
		}
		String name = this.labelNames.get(l);
		if (name == null)
		{
			name = "l" + this.labelNames.size();
			this.labelNames.put(l, name);
			this.buf.append("Label ").append(name).append(" = new Label();\n");
		}
	}
	
	/**
	 * Appends the name of the given label to {@link #buf buf}. The given label
	 * <i>must</i> already have a name. One way to ensure this is to always call
	 * {@link #declareLabel declared} before calling this method.
	 * 
	 * @param l
	 *            a label.
	 */
	protected void appendLabel(final Label l)
	{
		this.buf.append(this.labelNames.get(l));
	}
}
