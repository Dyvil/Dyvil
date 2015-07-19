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
import dyvil.tools.asm.signature.SignatureReader;

/**
 * A {@link Printer} that prints a disassembled view of the classes it visits.
 *
 * @author Eric Bruneton
 */
public class Textifier extends Printer
{
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for internal
	 * type names in bytecode notation.
	 */
	public static final int			INTERNAL_NAME			= 0;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for field
	 * descriptors, formatted in bytecode notation
	 */
	public static final int			FIELD_DESCRIPTOR		= 1;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for field
	 * signatures, formatted in bytecode notation
	 */
	public static final int			FIELD_SIGNATURE			= 2;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for method
	 * descriptors, formatted in bytecode notation
	 */
	public static final int			METHOD_DESCRIPTOR		= 3;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for method
	 * signatures, formatted in bytecode notation
	 */
	public static final int			METHOD_SIGNATURE		= 4;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for class
	 * signatures, formatted in bytecode notation
	 */
	public static final int			CLASS_SIGNATURE			= 5;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for field or
	 * method return value signatures, formatted in default Java notation
	 * (non-bytecode)
	 */
	public static final int			TYPE_DECLARATION		= 6;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for class
	 * signatures, formatted in default Java notation (non-bytecode)
	 */
	public static final int			CLASS_DECLARATION		= 7;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for method
	 * parameter signatures, formatted in default Java notation (non-bytecode)
	 */
	public static final int			PARAMETERS_DECLARATION	= 8;
	
	/**
	 * Constant used in {@link #appendDescriptor appendDescriptor} for handle
	 * descriptors, formatted in bytecode notation
	 */
	public static final int			HANDLE_DESCRIPTOR		= 9;
	
	/**
	 * Tab for class members.
	 */
	protected String				tab						= "  ";
	
	/**
	 * Tab for bytecode instructions.
	 */
	protected String				tab2					= "    ";
	
	/**
	 * Tab for table and lookup switch instructions.
	 */
	protected String				tab3					= "      ";
	
	/**
	 * Tab for labels.
	 */
	protected String				ltab					= "   ";
	
	/**
	 * The label names. This map associate String values to Label keys.
	 */
	protected Map<Label, String>	labelNames;
	
	/**
	 * Class access flags
	 */
	private int						access;
	
	private int						valueNumber				= 0;
	
	/**
	 * Constructs a new {@link Textifier}. <i>Subclasses must not use this
	 * constructor</i>. Instead, they must use the {@link #Textifier(int)}
	 * version.
	 *
	 * @throws IllegalStateException
	 *             If a subclass calls this constructor.
	 */
	public Textifier()
	{
		this(Opcodes.ASM5);
		if (this.getClass() != Textifier.class)
		{
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Constructs a new {@link Textifier}.
	 *
	 * @param api
	 *            the ASM API version implemented by this visitor. Must be one
	 *            of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
	 */
	protected Textifier(final int api)
	{
		super(api);
	}
	
	/**
	 * Prints a disassembled view of the given class to the standard output.
	 * <p>
	 * Usage: Textifier [-debug] &lt;binary class name or class file name &gt;
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
			System.err.println("Prints a disassembled view of the given class.");
			System.err.println("Usage: Textifier [-debug] " + "<fully qualified class name or class file name>");
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
		cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), flags);
	}
	
	// ------------------------------------------------------------------------
	// Classes
	// ------------------------------------------------------------------------
	
	@Override
	public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces)
	{
		this.access = access;
		int major = version & 0xFFFF;
		int minor = version >>> 16;
		this.buf.setLength(0);
		this.buf.append("// class version ").append(major).append('.').append(minor).append(" (").append(version).append(")\n");
		if ((access & Opcodes.ACC_DEPRECATED) != 0)
		{
			this.buf.append("// DEPRECATED\n");
		}
		this.buf.append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
		
		this.appendDescriptor(CLASS_SIGNATURE, signature);
		if (signature != null)
		{
			TraceSignatureVisitor sv = new TraceSignatureVisitor(access);
			SignatureReader r = new SignatureReader(signature);
			r.accept(sv);
			this.buf.append("// declaration: ").append(name).append(sv.getDeclaration()).append('\n');
		}
		
		this.appendAccess(access & ~Opcodes.ACC_SUPER);
		if ((access & Opcodes.ACC_ANNOTATION) != 0)
		{
			this.buf.append("@interface ");
		}
		else if ((access & Opcodes.ACC_INTERFACE) != 0)
		{
			this.buf.append("interface ");
		}
		else if ((access & Opcodes.ACC_ENUM) == 0)
		{
			this.buf.append("class ");
		}
		this.appendDescriptor(INTERNAL_NAME, name);
		
		if (superName != null && !"java/lang/Object".equals(superName))
		{
			this.buf.append(" extends ");
			this.appendDescriptor(INTERNAL_NAME, superName);
			this.buf.append(' ');
		}
		if (interfaces != null && interfaces.length > 0)
		{
			this.buf.append(" implements ");
			for (String interface1 : interfaces)
			{
				this.appendDescriptor(INTERNAL_NAME, interface1);
				this.buf.append(' ');
			}
		}
		this.buf.append(" {\n\n");
		
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitSource(final String file, final String debug)
	{
		this.buf.setLength(0);
		if (file != null)
		{
			this.buf.append(this.tab).append("// compiled from: ").append(file).append('\n');
		}
		if (debug != null)
		{
			this.buf.append(this.tab).append("// debug info: ").append(debug).append('\n');
		}
		if (this.buf.length() > 0)
		{
			this.text.add(this.buf.toString());
		}
	}
	
	@Override
	public void visitOuterClass(final String owner, final String name, final String desc)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append("OUTERCLASS ");
		this.appendDescriptor(INTERNAL_NAME, owner);
		this.buf.append(' ');
		if (name != null)
		{
			this.buf.append(name).append(' ');
		}
		this.appendDescriptor(METHOD_DESCRIPTOR, desc);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Textifier visitClassAnnotation(final String desc, final boolean visible)
	{
		this.text.add("\n");
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public Printer visitClassTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		this.text.add("\n");
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitClassAttribute(final Attribute attr)
	{
		this.text.add("\n");
		this.visitAttribute(attr);
	}
	
	@Override
	public void visitInnerClass(final String name, final String outerName, final String innerName, final int access)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append("// access flags 0x");
		this.buf.append(Integer.toHexString(access & ~Opcodes.ACC_SUPER).toUpperCase()).append('\n');
		this.buf.append(this.tab);
		this.appendAccess(access);
		this.buf.append("INNERCLASS ");
		this.appendDescriptor(INTERNAL_NAME, name);
		this.buf.append(' ');
		this.appendDescriptor(INTERNAL_NAME, outerName);
		this.buf.append(' ');
		this.appendDescriptor(INTERNAL_NAME, innerName);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Textifier visitField(final int access, final String name, final String desc, final String signature, final Object value)
	{
		this.buf.setLength(0);
		this.buf.append('\n');
		if ((access & Opcodes.ACC_DEPRECATED) != 0)
		{
			this.buf.append(this.tab).append("// DEPRECATED\n");
		}
		this.buf.append(this.tab).append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
		if (signature != null)
		{
			this.buf.append(this.tab);
			this.appendDescriptor(FIELD_SIGNATURE, signature);
			
			TraceSignatureVisitor sv = new TraceSignatureVisitor(0);
			SignatureReader r = new SignatureReader(signature);
			r.acceptType(sv);
			this.buf.append(this.tab).append("// declaration: ").append(sv.getDeclaration()).append('\n');
		}
		
		this.buf.append(this.tab);
		this.appendAccess(access);
		
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append(' ').append(name);
		if (value != null)
		{
			this.buf.append(" = ");
			if (value instanceof String)
			{
				this.buf.append('\"').append(value).append('\"');
			}
			else
			{
				this.buf.append(value);
			}
		}
		
		this.buf.append('\n');
		this.text.add(this.buf.toString());
		
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		return t;
	}
	
	@Override
	public Textifier visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions)
	{
		this.buf.setLength(0);
		this.buf.append('\n');
		if ((access & Opcodes.ACC_DEPRECATED) != 0)
		{
			this.buf.append(this.tab).append("// DEPRECATED\n");
		}
		this.buf.append(this.tab).append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
		
		if (signature != null)
		{
			this.buf.append(this.tab);
			this.appendDescriptor(METHOD_SIGNATURE, signature);
			
			TraceSignatureVisitor v = new TraceSignatureVisitor(0);
			SignatureReader r = new SignatureReader(signature);
			r.accept(v);
			String genericDecl = v.getDeclaration();
			String genericReturn = v.getReturnType();
			String genericExceptions = v.getExceptions();
			
			this.buf.append(this.tab).append("// declaration: ").append(genericReturn).append(' ').append(name).append(genericDecl);
			if (genericExceptions != null)
			{
				this.buf.append(" throws ").append(genericExceptions);
			}
			this.buf.append('\n');
		}
		
		this.buf.append(this.tab);
		this.appendAccess(access & ~Opcodes.ACC_VOLATILE);
		if ((access & Opcodes.ACC_NATIVE) != 0)
		{
			this.buf.append("native ");
		}
		if ((access & Opcodes.ACC_VARARGS) != 0)
		{
			this.buf.append("varargs ");
		}
		if ((access & Opcodes.ACC_BRIDGE) != 0)
		{
			this.buf.append("bridge ");
		}
		if ((this.access & Opcodes.ACC_INTERFACE) != 0 && (access & Opcodes.ACC_ABSTRACT) == 0 && (access & Opcodes.ACC_STATIC) == 0)
		{
			this.buf.append("default ");
		}
		
		this.buf.append(name);
		this.appendDescriptor(METHOD_DESCRIPTOR, desc);
		if (exceptions != null && exceptions.length > 0)
		{
			this.buf.append(" throws ");
			for (String exception : exceptions)
			{
				this.appendDescriptor(INTERNAL_NAME, exception);
				this.buf.append(' ');
			}
		}
		
		this.buf.append('\n');
		this.text.add(this.buf.toString());
		
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		return t;
	}
	
	@Override
	public void visitClassEnd()
	{
		this.text.add("}\n");
	}
	
	// ------------------------------------------------------------------------
	// Annotations
	// ------------------------------------------------------------------------
	
	@Override
	public void visit(final String name, final Object value)
	{
		this.buf.setLength(0);
		this.appendComa(this.valueNumber++);
		
		if (name != null)
		{
			this.buf.append(name).append('=');
		}
		
		if (value instanceof String)
		{
			this.visitString((String) value);
		}
		else if (value instanceof Type)
		{
			this.visitType((Type) value);
		}
		else if (value instanceof Byte)
		{
			this.visitByte(((Byte) value).byteValue());
		}
		else if (value instanceof Boolean)
		{
			this.visitBoolean(((Boolean) value).booleanValue());
		}
		else if (value instanceof Short)
		{
			this.visitShort(((Short) value).shortValue());
		}
		else if (value instanceof Character)
		{
			this.visitChar(((Character) value).charValue());
		}
		else if (value instanceof Integer)
		{
			this.visitInt(((Integer) value).intValue());
		}
		else if (value instanceof Float)
		{
			this.visitFloat(((Float) value).floatValue());
		}
		else if (value instanceof Long)
		{
			this.visitLong(((Long) value).longValue());
		}
		else if (value instanceof Double)
		{
			this.visitDouble(((Double) value).doubleValue());
		}
		else if (value.getClass().isArray())
		{
			this.buf.append('{');
			if (value instanceof byte[])
			{
				byte[] v = (byte[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitByte(v[i]);
				}
			}
			else if (value instanceof boolean[])
			{
				boolean[] v = (boolean[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitBoolean(v[i]);
				}
			}
			else if (value instanceof short[])
			{
				short[] v = (short[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitShort(v[i]);
				}
			}
			else if (value instanceof char[])
			{
				char[] v = (char[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitChar(v[i]);
				}
			}
			else if (value instanceof int[])
			{
				int[] v = (int[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitInt(v[i]);
				}
			}
			else if (value instanceof long[])
			{
				long[] v = (long[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitLong(v[i]);
				}
			}
			else if (value instanceof float[])
			{
				float[] v = (float[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitFloat(v[i]);
				}
			}
			else if (value instanceof double[])
			{
				double[] v = (double[]) value;
				for (int i = 0; i < v.length; i++)
				{
					this.appendComa(i);
					this.visitDouble(v[i]);
				}
			}
			this.buf.append('}');
		}
		
		this.text.add(this.buf.toString());
	}
	
	private void visitInt(final int value)
	{
		this.buf.append(value);
	}
	
	private void visitLong(final long value)
	{
		this.buf.append(value).append('L');
	}
	
	private void visitFloat(final float value)
	{
		this.buf.append(value).append('F');
	}
	
	private void visitDouble(final double value)
	{
		this.buf.append(value).append('D');
	}
	
	private void visitChar(final char value)
	{
		this.buf.append("(char)").append((int) value);
	}
	
	private void visitShort(final short value)
	{
		this.buf.append("(short)").append(value);
	}
	
	private void visitByte(final byte value)
	{
		this.buf.append("(byte)").append(value);
	}
	
	private void visitBoolean(final boolean value)
	{
		this.buf.append(value);
	}
	
	private void visitString(final String value)
	{
		appendString(this.buf, value);
	}
	
	private void visitType(final Type value)
	{
		this.buf.append(value.getClassName()).append(".class");
	}
	
	@Override
	public void visitEnum(final String name, final String desc, final String value)
	{
		this.buf.setLength(0);
		this.appendComa(this.valueNumber++);
		if (name != null)
		{
			this.buf.append(name).append('=');
		}
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('.').append(value);
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Textifier visitAnnotation(final String name, final String desc)
	{
		this.buf.setLength(0);
		this.appendComa(this.valueNumber++);
		if (name != null)
		{
			this.buf.append(name).append('=');
		}
		this.buf.append('@');
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.text.add(")");
		return t;
	}
	
	@Override
	public Textifier visitArray(final String name)
	{
		this.buf.setLength(0);
		this.appendComa(this.valueNumber++);
		if (name != null)
		{
			this.buf.append(name).append('=');
		}
		this.buf.append('{');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.text.add("}");
		return t;
	}
	
	@Override
	public void visitAnnotationEnd()
	{
	}
	
	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------
	
	@Override
	public Textifier visitFieldAnnotation(final String desc, final boolean visible)
	{
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public Printer visitFieldTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
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
	}
	
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------
	
	@Override
	public void visitParameter(final String name, final int access)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("// parameter ");
		this.appendAccess(access);
		this.buf.append(' ').append(name == null ? "<no name>" : name).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Textifier visitAnnotationDefault()
	{
		this.text.add(this.tab2 + "default=");
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.text.add("\n");
		return t;
	}
	
	@Override
	public Textifier visitMethodAnnotation(final String desc, final boolean visible)
	{
		return this.visitAnnotation(desc, visible);
	}
	
	@Override
	public Printer visitMethodTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public Textifier visitParameterAnnotation(final int parameter, final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append('@');
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.text.add(visible ? ") // parameter " : ") // invisible, parameter ");
		this.text.add(parameter);
		this.text.add("\n");
		return t;
	}
	
	@Override
	public void visitMethodAttribute(final Attribute attr)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append("ATTRIBUTE ");
		this.appendDescriptor(-1, attr.type);
		
		if (attr instanceof Textifiable)
		{
			((Textifiable) attr).textify(this.buf, this.labelNames);
		}
		else
		{
			this.buf.append(" : unknown\n");
		}
		
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitCode()
	{
	}
	
	@Override
	public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack)
	{
		this.buf.setLength(0);
		this.buf.append(this.ltab);
		this.buf.append("FRAME ");
		switch (type)
		{
		case Opcodes.F_NEW:
		case Opcodes.F_FULL:
			this.buf.append("FULL [");
			this.appendFrameTypes(nLocal, local);
			this.buf.append("] [");
			this.appendFrameTypes(nStack, stack);
			this.buf.append(']');
			break;
		case Opcodes.F_APPEND:
			this.buf.append("APPEND [");
			this.appendFrameTypes(nLocal, local);
			this.buf.append(']');
			break;
		case Opcodes.F_CHOP:
			this.buf.append("CHOP ").append(nLocal);
			break;
		case Opcodes.F_SAME:
			this.buf.append("SAME");
			break;
		case Opcodes.F_SAME1:
			this.buf.append("SAME1 ");
			this.appendFrameTypes(1, stack);
			break;
		}
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitInsn(final int opcode)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitIntInsn(final int opcode, final int operand)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ').append(opcode == Opcodes.NEWARRAY ? TYPES[operand] : Integer.toString(operand))
				.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitVarInsn(final int opcode, final int var)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ').append(var).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitTypeInsn(final int opcode, final String type)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ');
		this.appendDescriptor(INTERNAL_NAME, type);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ');
		this.appendDescriptor(INTERNAL_NAME, owner);
		this.buf.append('.').append(name).append(" : ");
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('\n');
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
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ');
		this.appendDescriptor(INTERNAL_NAME, owner);
		this.buf.append('.').append(name).append(' ');
		this.appendDescriptor(METHOD_DESCRIPTOR, desc);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("INVOKEDYNAMIC").append(' ');
		this.buf.append(name);
		this.appendDescriptor(METHOD_DESCRIPTOR, desc);
		this.buf.append(" [");
		this.buf.append('\n');
		this.buf.append(this.tab3);
		this.appendHandle(bsm);
		this.buf.append('\n');
		this.buf.append(this.tab3).append("// arguments:");
		if (bsmArgs.length == 0)
		{
			this.buf.append(" none");
		}
		else
		{
			this.buf.append('\n');
			for (Object cst : bsmArgs)
			{
				this.buf.append(this.tab3);
				if (cst instanceof String)
				{
					Printer.appendString(this.buf, (String) cst);
				}
				else if (cst instanceof Type)
				{
					Type type = (Type) cst;
					if (type.getSort() == Type.METHOD)
					{
						this.appendDescriptor(METHOD_DESCRIPTOR, type.getDescriptor());
					}
					else
					{
						this.buf.append(type.getDescriptor()).append(".class");
					}
				}
				else if (cst instanceof Handle)
				{
					this.appendHandle((Handle) cst);
				}
				else
				{
					this.buf.append(cst);
				}
				this.buf.append(", \n");
			}
			this.buf.setLength(this.buf.length() - 3);
		}
		this.buf.append('\n');
		this.buf.append(this.tab2).append("]\n");
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitJumpInsn(final int opcode, final Label label)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append(OPCODES[opcode]).append(' ');
		this.appendLabel(label);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLabel(final Label label)
	{
		this.buf.setLength(0);
		this.buf.append(this.ltab);
		this.appendLabel(label);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLdcInsn(final Object cst)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("LDC ");
		if (cst instanceof String)
		{
			Printer.appendString(this.buf, (String) cst);
		}
		else if (cst instanceof Type)
		{
			this.buf.append(((Type) cst).getDescriptor()).append(".class");
		}
		else
		{
			this.buf.append(cst);
		}
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitIincInsn(final int var, final int increment)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("IINC ").append(var).append(' ').append(increment).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("TABLESWITCH\n");
		for (int i = 0; i < labels.length; ++i)
		{
			this.buf.append(this.tab3).append(min + i).append(": ");
			this.appendLabel(labels[i]);
			this.buf.append('\n');
		}
		this.buf.append(this.tab3).append("default: ");
		this.appendLabel(dflt);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("LOOKUPSWITCH\n");
		for (int i = 0; i < labels.length; ++i)
		{
			this.buf.append(this.tab3).append(keys[i]).append(": ");
			this.appendLabel(labels[i]);
			this.buf.append('\n');
		}
		this.buf.append(this.tab3).append("default: ");
		this.appendLabel(dflt);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("MULTIANEWARRAY ");
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append(' ').append(dims).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Printer visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("TRYCATCHBLOCK ");
		this.appendLabel(start);
		this.buf.append(' ');
		this.appendLabel(end);
		this.buf.append(' ');
		this.appendLabel(handler);
		this.buf.append(' ');
		this.appendDescriptor(INTERNAL_NAME, type);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Printer visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("TRYCATCHBLOCK @");
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.buf.setLength(0);
		this.buf.append(") : ");
		this.appendTypeReference(typeRef);
		this.buf.append(", ").append(typePath);
		this.buf.append(visible ? "\n" : " // invisible\n");
		this.text.add(this.buf.toString());
		return t;
	}
	
	@Override
	public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("LOCALVARIABLE ").append(name).append(' ');
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append(' ');
		this.appendLabel(start);
		this.buf.append(' ');
		this.appendLabel(end);
		this.buf.append(' ').append(index).append('\n');
		
		if (signature != null)
		{
			this.buf.append(this.tab2);
			this.appendDescriptor(FIELD_SIGNATURE, signature);
			
			TraceSignatureVisitor sv = new TraceSignatureVisitor(0);
			SignatureReader r = new SignatureReader(signature);
			r.acceptType(sv);
			this.buf.append(this.tab2).append("// declaration: ").append(sv.getDeclaration()).append('\n');
		}
		this.text.add(this.buf.toString());
	}
	
	@Override
	public Printer visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("LOCALVARIABLE @");
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.buf.setLength(0);
		this.buf.append(") : ");
		this.appendTypeReference(typeRef);
		this.buf.append(", ").append(typePath);
		for (int i = 0; i < start.length; ++i)
		{
			this.buf.append(" [ ");
			this.appendLabel(start[i]);
			this.buf.append(" - ");
			this.appendLabel(end[i]);
			this.buf.append(" - ").append(index[i]).append(" ]");
		}
		this.buf.append(visible ? "\n" : " // invisible\n");
		this.text.add(this.buf.toString());
		return t;
	}
	
	@Override
	public void visitLineNumber(final int line, final Label start)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("LINENUMBER ").append(line).append(' ');
		this.appendLabel(start);
		this.buf.append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMaxs(final int maxStack, final int maxLocals)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("MAXSTACK = ").append(maxStack).append('\n');
		this.text.add(this.buf.toString());
		
		this.buf.setLength(0);
		this.buf.append(this.tab2).append("MAXLOCALS = ").append(maxLocals).append('\n');
		this.text.add(this.buf.toString());
	}
	
	@Override
	public void visitMethodEnd()
	{
	}
	
	// ------------------------------------------------------------------------
	// Common methods
	// ------------------------------------------------------------------------
	
	/**
	 * Prints a disassembled view of the given annotation.
	 *
	 * @param desc
	 *            the class descriptor of the annotation class.
	 * @param visible
	 *            <tt>true</tt> if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values.
	 */
	public Textifier visitAnnotation(final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append('@');
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.text.add(visible ? ")\n" : ") // invisible\n");
		return t;
	}
	
	/**
	 * Prints a disassembled view of the given type annotation.
	 *
	 * @param typeRef
	 *            a reference to the annotated type. See {@link TypeReference}.
	 * @param typePath
	 *            the path to the annotated type argument, wildcard bound, array
	 *            element type, or static inner type within 'typeRef'. May be
	 *            <tt>null</tt> if the annotation targets 'typeRef' as a whole.
	 * @param desc
	 *            the class descriptor of the annotation class.
	 * @param visible
	 *            <tt>true</tt> if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values.
	 */
	public Textifier visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append('@');
		this.appendDescriptor(FIELD_DESCRIPTOR, desc);
		this.buf.append('(');
		this.text.add(this.buf.toString());
		Textifier t = this.createTextifier();
		this.text.add(t.getText());
		this.buf.setLength(0);
		this.buf.append(") : ");
		this.appendTypeReference(typeRef);
		this.buf.append(", ").append(typePath);
		this.buf.append(visible ? "\n" : " // invisible\n");
		this.text.add(this.buf.toString());
		return t;
	}
	
	/**
	 * Prints a disassembled view of the given attribute.
	 *
	 * @param attr
	 *            an attribute.
	 */
	public void visitAttribute(final Attribute attr)
	{
		this.buf.setLength(0);
		this.buf.append(this.tab).append("ATTRIBUTE ");
		this.appendDescriptor(-1, attr.type);
		
		if (attr instanceof Textifiable)
		{
			((Textifiable) attr).textify(this.buf, null);
		}
		else
		{
			this.buf.append(" : unknown\n");
		}
		
		this.text.add(this.buf.toString());
	}
	
	// ------------------------------------------------------------------------
	// Utility methods
	// ------------------------------------------------------------------------
	
	/**
	 * Creates a new TraceVisitor instance.
	 *
	 * @return a new TraceVisitor.
	 */
	protected Textifier createTextifier()
	{
		return new Textifier();
	}
	
	/**
	 * Appends an internal name, a type descriptor or a type signature to
	 * {@link #buf buf}.
	 *
	 * @param type
	 *            indicates if desc is an internal name, a field descriptor, a
	 *            method descriptor, a class signature, ...
	 * @param desc
	 *            an internal name, type descriptor, or type signature. May be
	 *            <tt>null</tt>.
	 */
	protected void appendDescriptor(final int type, final String desc)
	{
		if (type == CLASS_SIGNATURE || type == FIELD_SIGNATURE || type == METHOD_SIGNATURE)
		{
			if (desc != null)
			{
				this.buf.append("// signature ").append(desc).append('\n');
			}
		}
		else
		{
			this.buf.append(desc);
		}
	}
	
	/**
	 * Appends the name of the given label to {@link #buf buf}. Creates a new
	 * label name if the given label does not yet have one.
	 *
	 * @param l
	 *            a label.
	 */
	protected void appendLabel(final Label l)
	{
		if (this.labelNames == null)
		{
			this.labelNames = new HashMap<Label, String>();
		}
		String name = this.labelNames.get(l);
		if (name == null)
		{
			name = "L" + this.labelNames.size();
			this.labelNames.put(l, name);
		}
		this.buf.append(name);
	}
	
	/**
	 * Appends the information about the given handle to {@link #buf buf}.
	 *
	 * @param h
	 *            a handle, non null.
	 */
	protected void appendHandle(final Handle h)
	{
		int tag = h.getTag();
		this.buf.append("// handle kind 0x").append(Integer.toHexString(tag)).append(" : ");
		boolean isMethodHandle = false;
		switch (tag)
		{
		case Opcodes.H_GETFIELD:
			this.buf.append("GETFIELD");
			break;
		case Opcodes.H_GETSTATIC:
			this.buf.append("GETSTATIC");
			break;
		case Opcodes.H_PUTFIELD:
			this.buf.append("PUTFIELD");
			break;
		case Opcodes.H_PUTSTATIC:
			this.buf.append("PUTSTATIC");
			break;
		case Opcodes.H_INVOKEINTERFACE:
			this.buf.append("INVOKEINTERFACE");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKESPECIAL:
			this.buf.append("INVOKESPECIAL");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKESTATIC:
			this.buf.append("INVOKESTATIC");
			isMethodHandle = true;
			break;
		case Opcodes.H_INVOKEVIRTUAL:
			this.buf.append("INVOKEVIRTUAL");
			isMethodHandle = true;
			break;
		case Opcodes.H_NEWINVOKESPECIAL:
			this.buf.append("NEWINVOKESPECIAL");
			isMethodHandle = true;
			break;
		}
		this.buf.append('\n');
		this.buf.append(this.tab3);
		this.appendDescriptor(INTERNAL_NAME, h.getOwner());
		this.buf.append('.');
		this.buf.append(h.getName());
		if (!isMethodHandle)
		{
			this.buf.append('(');
		}
		this.appendDescriptor(HANDLE_DESCRIPTOR, h.getDesc());
		if (!isMethodHandle)
		{
			this.buf.append(')');
		}
	}
	
	/**
	 * Appends a string representation of the given access modifiers to
	 * {@link #buf buf}.
	 *
	 * @param access
	 *            some access modifiers.
	 */
	private void appendAccess(final int access)
	{
		if ((access & Opcodes.ACC_PUBLIC) != 0)
		{
			this.buf.append("public ");
		}
		if ((access & Opcodes.ACC_PRIVATE) != 0)
		{
			this.buf.append("private ");
		}
		if ((access & Opcodes.ACC_PROTECTED) != 0)
		{
			this.buf.append("protected ");
		}
		if ((access & Opcodes.ACC_FINAL) != 0)
		{
			this.buf.append("final ");
		}
		if ((access & Opcodes.ACC_STATIC) != 0)
		{
			this.buf.append("static ");
		}
		if ((access & Opcodes.ACC_SYNCHRONIZED) != 0)
		{
			this.buf.append("synchronized ");
		}
		if ((access & Opcodes.ACC_VOLATILE) != 0)
		{
			this.buf.append("volatile ");
		}
		if ((access & Opcodes.ACC_TRANSIENT) != 0)
		{
			this.buf.append("transient ");
		}
		if ((access & Opcodes.ACC_ABSTRACT) != 0)
		{
			this.buf.append("abstract ");
		}
		if ((access & Opcodes.ACC_STRICT) != 0)
		{
			this.buf.append("strictfp ");
		}
		if ((access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			this.buf.append("synthetic ");
		}
		if ((access & Opcodes.ACC_MANDATED) != 0)
		{
			this.buf.append("mandated ");
		}
		if ((access & Opcodes.ACC_ENUM) != 0)
		{
			this.buf.append("enum ");
		}
	}
	
	private void appendComa(final int i)
	{
		if (i != 0)
		{
			this.buf.append(", ");
		}
	}
	
	private void appendTypeReference(final int typeRef)
	{
		TypeReference ref = new TypeReference(typeRef);
		switch (ref.getSort())
		{
		case TypeReference.CLASS_TYPE_PARAMETER:
			this.buf.append("CLASS_TYPE_PARAMETER ").append(ref.getTypeParameterIndex());
			break;
		case TypeReference.METHOD_TYPE_PARAMETER:
			this.buf.append("METHOD_TYPE_PARAMETER ").append(ref.getTypeParameterIndex());
			break;
		case TypeReference.CLASS_EXTENDS:
			this.buf.append("CLASS_EXTENDS ").append(ref.getSuperTypeIndex());
			break;
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
			this.buf.append("CLASS_TYPE_PARAMETER_BOUND ").append(ref.getTypeParameterIndex()).append(", ").append(ref.getTypeParameterBoundIndex());
			break;
		case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
			this.buf.append("METHOD_TYPE_PARAMETER_BOUND ").append(ref.getTypeParameterIndex()).append(", ").append(ref.getTypeParameterBoundIndex());
			break;
		case TypeReference.FIELD:
			this.buf.append("FIELD");
			break;
		case TypeReference.METHOD_RETURN:
			this.buf.append("METHOD_RETURN");
			break;
		case TypeReference.METHOD_RECEIVER:
			this.buf.append("METHOD_RECEIVER");
			break;
		case TypeReference.METHOD_FORMAL_PARAMETER:
			this.buf.append("METHOD_FORMAL_PARAMETER ").append(ref.getFormalParameterIndex());
			break;
		case TypeReference.THROWS:
			this.buf.append("THROWS ").append(ref.getExceptionIndex());
			break;
		case TypeReference.LOCAL_VARIABLE:
			this.buf.append("LOCAL_VARIABLE");
			break;
		case TypeReference.RESOURCE_VARIABLE:
			this.buf.append("RESOURCE_VARIABLE");
			break;
		case TypeReference.EXCEPTION_PARAMETER:
			this.buf.append("EXCEPTION_PARAMETER ").append(ref.getTryCatchBlockIndex());
			break;
		case TypeReference.INSTANCEOF:
			this.buf.append("INSTANCEOF");
			break;
		case TypeReference.NEW:
			this.buf.append("NEW");
			break;
		case TypeReference.CONSTRUCTOR_REFERENCE:
			this.buf.append("CONSTRUCTOR_REFERENCE");
			break;
		case TypeReference.METHOD_REFERENCE:
			this.buf.append("METHOD_REFERENCE");
			break;
		case TypeReference.CAST:
			this.buf.append("CAST ").append(ref.getTypeArgumentIndex());
			break;
		case TypeReference.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
			this.buf.append("CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
			break;
		case TypeReference.METHOD_INVOCATION_TYPE_ARGUMENT:
			this.buf.append("METHOD_INVOCATION_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
			break;
		case TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
			this.buf.append("CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
			break;
		case TypeReference.METHOD_REFERENCE_TYPE_ARGUMENT:
			this.buf.append("METHOD_REFERENCE_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
			break;
		}
	}
	
	private void appendFrameTypes(final int n, final Object[] o)
	{
		for (int i = 0; i < n; ++i)
		{
			if (i > 0)
			{
				this.buf.append(' ');
			}
			if (o[i] instanceof String)
			{
				String desc = (String) o[i];
				if (desc.startsWith("["))
				{
					this.appendDescriptor(FIELD_DESCRIPTOR, desc);
				}
				else
				{
					this.appendDescriptor(INTERNAL_NAME, desc);
				}
			}
			else if (o[i] instanceof Integer)
			{
				switch (((Integer) o[i]).intValue())
				{
				case 0:
					this.appendDescriptor(FIELD_DESCRIPTOR, "T");
					break;
				case 1:
					this.appendDescriptor(FIELD_DESCRIPTOR, "I");
					break;
				case 2:
					this.appendDescriptor(FIELD_DESCRIPTOR, "F");
					break;
				case 3:
					this.appendDescriptor(FIELD_DESCRIPTOR, "D");
					break;
				case 4:
					this.appendDescriptor(FIELD_DESCRIPTOR, "J");
					break;
				case 5:
					this.appendDescriptor(FIELD_DESCRIPTOR, "N");
					break;
				case 6:
					this.appendDescriptor(FIELD_DESCRIPTOR, "U");
					break;
				}
			}
			else
			{
				this.appendLabel((Label) o[i]);
			}
		}
	}
}
