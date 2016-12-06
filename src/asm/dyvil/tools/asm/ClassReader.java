/*
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

import java.io.IOException;
import java.io.InputStream;

public class ClassReader
{
	public static final int SKIP_CODE     = 1;
	public static final int SKIP_DEBUG    = 2;
	public static final int SKIP_FRAMES   = 4;
	public static final int EXPAND_FRAMES = 8;
	public final  byte[]   b;
	private final int[]    items;
	private final String[] strings;
	private final int      maxStringLength;
	public final  int      header;

	public ClassReader(final byte[] b)
	{
		this(b, 0, b.length);
	}

	public ClassReader(final byte[] b, final int off, @SuppressWarnings("UnusedParameters") final int len)
	{
		this.b = b;
		// checks the class version
		if (this.readShort(off + 6) > ASMConstants.V1_8)
		{
			throw new IllegalArgumentException();
		}
		// parses the constant pool
		this.items = new int[this.readUnsignedShort(off + 8)];
		int n = this.items.length;
		this.strings = new String[n];
		int max = 0;
		int index = off + 10;
		for (int i = 1; i < n; ++i)
		{
			this.items[i] = index + 1;
			int size;
			switch (b[index])
			{
			case ClassWriter.FIELD:
			case ClassWriter.METH:
			case ClassWriter.IMETH:
			case ClassWriter.INT:
			case ClassWriter.FLOAT:
			case ClassWriter.NAME_TYPE:
			case ClassWriter.INDY:
				size = 5;
				break;
			case ClassWriter.LONG:
			case ClassWriter.DOUBLE:
				size = 9;
				++i;
				break;
			case ClassWriter.UTF8:
				size = 3 + this.readUnsignedShort(index + 1);
				if (size > max)
				{
					max = size;
				}
				break;
			case ClassWriter.HANDLE:
				size = 4;
				break;
			// case ClassWriter.CLASS:
			// case ClassWriter.STR:
			// case ClassWriter.MTYPE
			default:
				size = 3;
				break;
			}
			index += size;
		}
		this.maxStringLength = max;
		// the class header information starts just after the constant pool
		this.header = index;
	}

	public int getAccess()
	{
		return this.readUnsignedShort(this.header);
	}

	public String getClassName()
	{
		return this.readClass(this.header + 2, new char[this.maxStringLength]);
	}

	public String getSuperName()
	{
		return this.readClass(this.header + 4, new char[this.maxStringLength]);
	}

	public String[] getInterfaces()
	{
		int index = this.header + 6;
		int n = this.readUnsignedShort(index);
		String[] interfaces = new String[n];
		if (n > 0)
		{
			char[] buf = new char[this.maxStringLength];
			for (int i = 0; i < n; ++i)
			{
				index += 2;
				interfaces[i] = this.readClass(index, buf);
			}
		}
		return interfaces;
	}

	void copyPool(final ClassWriter classWriter)
	{
		char[] buf = new char[this.maxStringLength];
		int ll = this.items.length;
		Item[] items2 = new Item[ll];
		for (int i = 1; i < ll; i++)
		{
			int index = this.items[i];
			int tag = this.b[index - 1];
			Item item = new Item(i);
			int nameType;
			switch (tag)
			{
			case ClassWriter.FIELD:
			case ClassWriter.METH:
			case ClassWriter.IMETH:
				nameType = this.items[this.readUnsignedShort(index + 2)];
				item.set(tag, this.readClass(index, buf), this.readUTF8(nameType, buf),
				         this.readUTF8(nameType + 2, buf));
				break;
			case ClassWriter.INT:
				item.set(this.readInt(index));
				break;
			case ClassWriter.FLOAT:
				item.set(Float.intBitsToFloat(this.readInt(index)));
				break;
			case ClassWriter.NAME_TYPE:
				item.set(tag, this.readUTF8(index, buf), this.readUTF8(index + 2, buf), null);
				break;
			case ClassWriter.LONG:
				item.set(this.readLong(index));
				++i;
				break;
			case ClassWriter.DOUBLE:
				item.set(Double.longBitsToDouble(this.readLong(index)));
				++i;
				break;
			case ClassWriter.UTF8:
			{
				String s = this.strings[i];
				if (s == null)
				{
					index = this.items[i];
					s = this.strings[i] = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
				}
				item.set(tag, s, null, null);
				break;
			}
			case ClassWriter.HANDLE:
			{
				int fieldOrMethodRef = this.items[this.readUnsignedShort(index + 1)];
				nameType = this.items[this.readUnsignedShort(fieldOrMethodRef + 2)];
				item.set(ClassWriter.HANDLE_BASE + this.readByte(index), this.readClass(fieldOrMethodRef, buf),
				         this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf));
				break;
			}
			case ClassWriter.INDY:
				if (classWriter.bootstrapMethods == null)
				{
					this.copyBootstrapMethods(classWriter, items2, buf);
				}
				nameType = this.items[this.readUnsignedShort(index + 2)];
				item.set(this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf), this.readUnsignedShort(index));
				break;
			// case ClassWriter.STR:
			// case ClassWriter.CLASS:
			// case ClassWriter.MTYPE
			default:
				item.set(tag, this.readUTF8(index, buf), null, null);
				break;
			}

			int index2 = item.hashCode % items2.length;
			item.next = items2[index2];
			items2[index2] = item;
		}

		int off = this.items[1] - 1;
		classWriter.pool.putByteArray(this.b, off, this.header - off);
		classWriter.items = items2;
		classWriter.threshold = (int) (0.75d * ll);
		classWriter.index = ll;
	}

	private void copyBootstrapMethods(final ClassWriter classWriter, final Item[] items, final char[] c)
	{
		// finds the "BootstrapMethods" attribute
		int u = this.getAttributes();
		boolean found = false;
		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			String attrName = this.readUTF8(u + 2, c);
			if ("BootstrapMethods".equals(attrName))
			{
				found = true;
				break;
			}
			u += 6 + this.readInt(u + 4);
		}
		if (!found)
		{
			return;
		}
		// copies the bootstrap methods in the class writer
		int boostrapMethodCount = this.readUnsignedShort(u + 8);
		for (int j = 0, v = u + 10; j < boostrapMethodCount; j++)
		{
			int position = v - u - 10;
			int hashCode = this.readConst(this.readUnsignedShort(v), c).hashCode();
			for (int k = this.readUnsignedShort(v + 2); k > 0; --k)
			{
				hashCode ^= this.readConst(this.readUnsignedShort(v + 4), c).hashCode();
				v += 2;
			}
			v += 4;
			Item item = new Item(j);
			item.set(position, hashCode & 0x7FFFFFFF);
			int index = item.hashCode % items.length;
			item.next = items[index];
			items[index] = item;
		}
		int attrSize = this.readInt(u + 4);
		ByteVector bootstrapMethods = new ByteVector(attrSize + 62);
		bootstrapMethods.putByteArray(this.b, u + 10, attrSize - 2);
		classWriter.bootstrapMethodsCount = boostrapMethodCount;
		classWriter.bootstrapMethods = bootstrapMethods;
	}

	public ClassReader(final InputStream is) throws IOException
	{
		this(readClass(is, false));
	}

	public ClassReader(final String name) throws IOException
	{
		this(readClass(ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class"), true));
	}

	private static byte[] readClass(final InputStream is, boolean close) throws IOException
	{
		if (is == null)
		{
			throw new IOException("Class not found");
		}
		try
		{
			byte[] b = new byte[is.available()];
			int len = 0;
			while (true)
			{
				int n = is.read(b, len, b.length - len);
				if (n == -1)
				{
					if (len < b.length)
					{
						byte[] c = new byte[len];
						System.arraycopy(b, 0, c, 0, len);
						b = c;
					}
					return b;
				}
				len += n;
				if (len == b.length)
				{
					int last = is.read();
					if (last < 0)
					{
						return b;
					}
					byte[] c = new byte[b.length + 1000];
					System.arraycopy(b, 0, c, 0, len);
					c[len++] = (byte) last;
					b = c;
				}
			}
		}
		finally
		{
			if (close)
			{
				is.close();
			}
		}
	}

	public void accept(final ClassVisitor classVisitor, final int flags)
	{
		this.accept(classVisitor, new Attribute[0], flags);
	}

	@SuppressWarnings("ConstantConditions")
	public void accept(final ClassVisitor classVisitor, final Attribute[] attrs, final int flags)
	{
		int u = this.header; // current offset in the class file
		char[] c = new char[this.maxStringLength]; // buffer used to read
		// strings

		Context context = new Context();
		context.attrs = attrs;
		context.flags = flags;
		context.buffer = c;

		// reads the class declaration
		int access = this.readUnsignedShort(u);
		String name = this.readClass(u + 2, c);
		String superClass = this.readClass(u + 4, c);
		String[] interfaces = new String[this.readUnsignedShort(u + 6)];
		u += 8;
		for (int i = 0; i < interfaces.length; ++i)
		{
			interfaces[i] = this.readClass(u, c);
			u += 2;
		}

		// reads the class attributes
		String signature = null;
		String sourceFile = null;
		String sourceDebug = null;
		String enclosingOwner = null;
		String enclosingName = null;
		String enclosingDesc = null;
		int anns = 0;
		int ianns = 0;
		int tanns = 0;
		int itanns = 0;
		int innerClasses = 0;
		Attribute attributes = null;

		u = this.getAttributes();
		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			String attrName = this.readUTF8(u + 2, c);
			// tests are sorted in decreasing frequency order
			// (based on frequencies observed on typical classes)
			switch (attrName)
			{
			case "SourceFile":
				sourceFile = this.readUTF8(u + 8, c);
				break;
			case "InnerClasses":
				innerClasses = u + 8;
				break;
			case "EnclosingMethod":
				enclosingOwner = this.readClass(u + 8, c);
				int item = this.readUnsignedShort(u + 10);
				if (item != 0)
				{
					enclosingName = this.readUTF8(this.items[item], c);
					enclosingDesc = this.readUTF8(this.items[item] + 2, c);
				}
				break;
			case "Signature":
				signature = this.readUTF8(u + 8, c);
				break;
			case "RuntimeVisibleAnnotations":
				anns = u + 8;
				break;
			case "RuntimeVisibleTypeAnnotations":
				tanns = u + 8;
				break;
			case "Deprecated":
				access |= ASMConstants.ACC_DEPRECATED;
				break;
			case "Synthetic":
				access |= ASMConstants.ACC_SYNTHETIC | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE;
				break;
			case "SourceDebugExtension":
				int len = this.readInt(u + 4);
				sourceDebug = this.readUTF(u + 8, len, new char[len]);
				break;
			case "RuntimeInvisibleAnnotations":
				ianns = u + 8;
				break;
			case "RuntimeInvisibleTypeAnnotations":
				itanns = u + 8;
				break;
			case "BootstrapMethods":
				int[] bootstrapMethods = new int[this.readUnsignedShort(u + 8)];
				for (int j = 0, v = u + 10; j < bootstrapMethods.length; j++)
				{
					bootstrapMethods[j] = v;
					v += 2 + this.readUnsignedShort(v + 2) << 1;
				}
				context.bootstrapMethods = bootstrapMethods;
				break;
			default:
				Attribute attr = this.readAttribute(attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
				if (attr != null)
				{
					attr.next = attributes;
					attributes = attr;
				}
				break;
			}
			u += 6 + this.readInt(u + 4);
		}

		// visits the class declaration
		classVisitor.visit(this.readInt(this.items[1] - 7), access, name, signature, superClass, interfaces);

		// visits the source and debug info
		if ((flags & SKIP_DEBUG) == 0 && (sourceFile != null || sourceDebug != null))
		{
			classVisitor.visitSource(sourceFile, sourceDebug);
		}

		// visits the outer class
		if (enclosingOwner != null)
		{
			classVisitor.visitOuterClass(enclosingOwner, enclosingName, enclosingDesc);
		}

		// visits the class annotations and type annotations
		if (anns != 0)
		{
			for (int i = this.readUnsignedShort(anns), v = anns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(v, c), true));
			}
		}
		if (ianns != 0)
		{
			for (int i = this.readUnsignedShort(ianns), v = ianns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(v, c), false));
			}
		}
		if (tanns != 0)
		{
			for (int i = this.readUnsignedShort(tanns), v = tanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef,
				                                                                               context.typePath,
				                                                                               this.readUTF8(v, c),
				                                                                               true));
			}
		}
		if (itanns != 0)
		{
			for (int i = this.readUnsignedShort(itanns), v = itanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef,
				                                                                               context.typePath,
				                                                                               this.readUTF8(v, c),
				                                                                               false));
			}
		}

		// visits the attributes
		while (attributes != null)
		{
			Attribute attr = attributes.next;
			attributes.next = null;
			classVisitor.visitAttribute(attributes);
			attributes = attr;
		}

		// visits the inner classes
		if (innerClasses != 0)
		{
			int v = innerClasses + 2;
			for (int i = this.readUnsignedShort(innerClasses); i > 0; --i)
			{
				classVisitor.visitInnerClass(this.readClass(v, c), this.readClass(v + 2, c), this.readUTF8(v + 4, c),
				                             this.readUnsignedShort(v + 6));
				v += 8;
			}
		}

		// visits the fields and methods
		u = this.header + 10 + 2 * interfaces.length;
		for (int i = this.readUnsignedShort(u - 2); i > 0; --i)
		{
			u = this.readField(classVisitor, context, u);
		}
		u += 2;
		for (int i = this.readUnsignedShort(u - 2); i > 0; --i)
		{
			u = this.readMethod(classVisitor, context, u);
		}

		// visits the end of the class
		classVisitor.visitEnd();
	}

	private int readField(final ClassVisitor classVisitor, final Context context, int u)
	{
		// reads the field declaration
		char[] c = context.buffer;
		int access = this.readUnsignedShort(u);
		String name = this.readUTF8(u + 2, c);
		String desc = this.readUTF8(u + 4, c);
		u += 6;

		// reads the field attributes
		String signature = null;
		int anns = 0;
		int ianns = 0;
		int tanns = 0;
		int itanns = 0;
		Object value = null;
		Attribute attributes = null;

		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			final String attrName = this.readUTF8(u + 2, c);
			// tests are sorted in decreasing frequency order
			// (based on frequencies observed on typical classes)
			switch (attrName)
			{
			case "ConstantValue":
				int item = this.readUnsignedShort(u + 8);
				value = item == 0 ? null : this.readConst(item, c);
				break;
			case "Signature":
				signature = this.readUTF8(u + 8, c);
				break;
			case "Deprecated":
				access |= ASMConstants.ACC_DEPRECATED;
				break;
			case "Synthetic":
				access |= ASMConstants.ACC_SYNTHETIC | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE;
				break;
			case "RuntimeVisibleAnnotations":
				anns = u + 8;
				break;
			case "RuntimeVisibleTypeAnnotations":
				tanns = u + 8;
				break;
			case "RuntimeInvisibleAnnotations":
				ianns = u + 8;
				break;
			case "RuntimeInvisibleTypeAnnotations":
				itanns = u + 8;
				break;
			default:
				Attribute attr = this.readAttribute(context.attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
				if (attr != null)
				{
					attr.next = attributes;
					attributes = attr;
				}
				break;
			}
			u += 6 + this.readInt(u + 4);
		}
		u += 2;

		// visits the field declaration
		FieldVisitor fv = classVisitor.visitField(access, name, desc, signature, value);
		if (fv == null)
		{
			return u;
		}

		// visits the field annotations and type annotations
		if (anns != 0)
		{
			for (int i = this.readUnsignedShort(anns), v = anns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, fv.visitAnnotation(this.readUTF8(v, c), true));
			}
		}
		if (ianns != 0)
		{
			for (int i = this.readUnsignedShort(ianns), v = ianns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, fv.visitAnnotation(this.readUTF8(v, c), false));
			}
		}
		if (tanns != 0)
		{
			for (int i = this.readUnsignedShort(tanns), v = tanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, fv.visitTypeAnnotation(context.typeRef, context.typePath,
				                                                                     this.readUTF8(v, c), true));
			}
		}
		if (itanns != 0)
		{
			for (int i = this.readUnsignedShort(itanns), v = itanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, fv.visitTypeAnnotation(context.typeRef, context.typePath,
				                                                                     this.readUTF8(v, c), false));
			}
		}

		// visits the field attributes
		while (attributes != null)
		{
			Attribute attr = attributes.next;
			attributes.next = null;
			fv.visitAttribute(attributes);
			attributes = attr;
		}

		// visits the end of the field
		fv.visitEnd();

		return u;
	}

	@SuppressWarnings("ConstantConditions")
	private int readMethod(final ClassVisitor classVisitor, final Context context, int u)
	{
		// reads the method declaration
		char[] c = context.buffer;
		context.access = this.readUnsignedShort(u);
		context.name = this.readUTF8(u + 2, c);
		context.desc = this.readUTF8(u + 4, c);
		u += 6;

		// reads the method attributes
		int code = 0;
		int exception = 0;
		String[] exceptions = null;
		String signature = null;
		int methodParameters = 0;
		// int variables = 0;
		int anns = 0;
		int ianns = 0;
		int tanns = 0;
		int itanns = 0;
		int dann = 0;
		int mpanns = 0;
		int impanns = 0;
		int firstAttribute = u;
		Attribute attributes = null;

		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			String attrName = this.readUTF8(u + 2, c);
			switch (attrName)
			{
			case "Code":
				if ((context.flags & SKIP_CODE) == 0)
				{
					code = u + 8;
				}
				break;
			case "Exceptions":
				exceptions = new String[this.readUnsignedShort(u + 8)];
				exception = u + 10;
				for (int j = 0; j < exceptions.length; ++j)
				{
					exceptions[j] = this.readClass(exception, c);
					exception += 2;
				}
				break;
			case "Signature":
				signature = this.readUTF8(u + 8, c);
				break;
			case "Deprecated":
				context.access |= ASMConstants.ACC_DEPRECATED;
				break;
			case "RuntimeVisibleAnnotations":
				anns = u + 8;
				break;
			case "RuntimeVisibleTypeAnnotations":
				tanns = u + 8;
				break;
			case "AnnotationDefault":
				dann = u + 8;
				break;
			case "Synthetic":
				context.access |= ASMConstants.ACC_SYNTHETIC | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE;
				break;
			case "RuntimeInvisibleAnnotations":
				ianns = u + 8;
				break;
			case "RuntimeInvisibleTypeAnnotations":
				itanns = u + 8;
				break;
			case "RuntimeVisibleParameterAnnotations":
				mpanns = u + 8;
				break;
			case "RuntimeInvisibleParameterAnnotations":
				impanns = u + 8;
				break;
			case "MethodParameters":
				methodParameters = u + 8;
				break;
			default:
				Attribute attr = this.readAttribute(context.attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
				if (attr != null)
				{
					attr.next = attributes;
					attributes = attr;
				}
				break;
			}
			u += 6 + this.readInt(u + 4);
		}
		u += 2;

		// visits the method declaration
		final MethodVisitor mv = classVisitor
			                         .visitMethod(context.access, context.name, context.desc, signature, exceptions);
		if (mv == null)
		{
			return u;
		}
		
		/*
		 * if the returned MethodVisitor is in fact a MethodWriter, it means
		 * there is no method adapter between the reader and the writer. If, in
		 * addition, the writer's constant pool was copied from this reader
		 * (mw.cw.cr == this), and the signature and exceptions of the method
		 * have not been changed, then it is possible to skip all visit events
		 * and just copy the original code of the method to the writer (the
		 * access, name and descriptor can have been changed, this is not
		 * important since they are not copied as is from the reader).
		 */
		if (mv instanceof MethodWriter)
		{
			MethodWriter mw = (MethodWriter) mv;
			//noinspection StringEquality
			if (mw.cw.cr == this && signature == mw.signature)
			{
				boolean sameExceptions = false;
				if (exceptions == null)
				{
					sameExceptions = mw.exceptionCount == 0;
				}
				else if (exceptions.length == mw.exceptionCount)
				{
					sameExceptions = true;
					for (int j = exceptions.length - 1; j >= 0; --j)
					{
						exception -= 2;
						if (mw.exceptions[j] != this.readUnsignedShort(exception))
						{
							sameExceptions = false;
							break;
						}
					}
				}
				if (sameExceptions)
				{
					/*
					 * we do not copy directly the code into MethodWriter to
					 * save a byte array copy operation. The real copy will be
					 * done in ClassWriter.toByteArray().
					 */
					mw.classReaderOffset = firstAttribute;
					mw.classReaderLength = u - firstAttribute;
					return u;
				}
			}
		}

		// visit the method parameters
		if (methodParameters != 0)
		{
			for (int i = this.b[methodParameters] & 0xFF, v = methodParameters + 1; i > 0; --i, v = v + 4)
			{
				mv.visitParameter(this.readUTF8(v, c), this.readUnsignedShort(v + 2));
			}
		}
		// Make sure to read parameter names, if available
		/* else if (variables != 0 && (context.flags & SKIP_DEBUG) == 0)
		{
			// First name at offset varTable + 6
			int index = variables + 6;
			for (int i = this.b[methodParameters] & 0xFF; i > 0; --i)
			{
				final String name = this.readUTF8(index, c);
				mv.visitParameter(name, 0);
				index += 10;
			}
		} */

		// visits the method annotations
		if (dann != 0)
		{
			AnnotationVisitor dv = mv.visitAnnotationDefault();
			this.readAnnotationValue(dann, c, null, dv);
			if (dv != null)
			{
				dv.visitEnd();
			}
		}
		if (anns != 0)
		{
			for (int i = this.readUnsignedShort(anns), v = anns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, mv.visitAnnotation(this.readUTF8(v, c), true));
			}
		}
		if (ianns != 0)
		{
			for (int i = this.readUnsignedShort(ianns), v = ianns + 2; i > 0; --i)
			{
				v = this.readAnnotationValues(v + 2, c, true, mv.visitAnnotation(this.readUTF8(v, c), false));
			}
		}
		if (tanns != 0)
		{
			for (int i = this.readUnsignedShort(tanns), v = tanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, mv.visitTypeAnnotation(context.typeRef, context.typePath,
				                                                                     this.readUTF8(v, c), true));
			}
		}
		if (itanns != 0)
		{
			for (int i = this.readUnsignedShort(itanns), v = itanns + 2; i > 0; --i)
			{
				v = this.readAnnotationTarget(context, v);
				v = this.readAnnotationValues(v + 2, c, true, mv.visitTypeAnnotation(context.typeRef, context.typePath,
				                                                                     this.readUTF8(v, c), false));
			}
		}
		if (mpanns != 0)
		{
			this.readParameterAnnotations(mv, context, mpanns, true);
		}
		if (impanns != 0)
		{
			this.readParameterAnnotations(mv, context, impanns, false);
		}

		// visits the method attributes
		while (attributes != null)
		{
			Attribute attr = attributes.next;
			attributes.next = null;
			mv.visitAttribute(attributes);
			attributes = attr;
		}

		// visits the method code
		if (code != 0 && mv.visitCode())
		{
			this.readCode(mv, context, code);
		}

		// visits the end of the method
		mv.visitEnd();

		return u;
	}

	@SuppressWarnings("ConstantConditions")
	private void readCode(final MethodVisitor mv, final Context context, int u)
	{
		// reads the header
		byte[] b = this.b;
		char[] c = context.buffer;
		int maxStack = this.readUnsignedShort(u);
		int maxLocals = this.readUnsignedShort(u + 2);
		int codeLength = this.readInt(u + 4);
		u += 8;

		// reads the bytecode to find the labels
		int codeStart = u;
		int codeEnd = u + codeLength;
		Label[] labels = context.labels = new Label[codeLength + 2];
		this.readLabel(codeLength + 1, labels);
		while (u < codeEnd)
		{
			int offset = u - codeStart;
			int opcode = b[u] & 0xFF;
			switch (ClassWriter.TYPE[opcode])
			{
			case ClassWriter.NOARG_INSN:
			case ClassWriter.IMPLVAR_INSN:
				u += 1;
				break;
			case ClassWriter.LABEL_INSN:
				this.readLabel(offset + this.readShort(u + 1), labels);
				u += 3;
				break;
			case ClassWriter.LABELW_INSN:
				this.readLabel(offset + this.readInt(u + 1), labels);
				u += 5;
				break;
			case ClassWriter.WIDE_INSN:
				opcode = b[u + 1] & 0xFF;
				if (opcode == ASMConstants.IINC)
				{
					u += 6;
				}
				else
				{
					u += 4;
				}
				break;
			case ClassWriter.TABL_INSN:
				// skips 0 to 3 padding bytes
				u = u + 4 - (offset & 3);
				// reads instruction
				this.readLabel(offset + this.readInt(u), labels);
				for (int i = this.readInt(u + 8) - this.readInt(u + 4) + 1; i > 0; --i)
				{
					this.readLabel(offset + this.readInt(u + 12), labels);
					u += 4;
				}
				u += 12;
				break;
			case ClassWriter.LOOK_INSN:
				// skips 0 to 3 padding bytes
				u = u + 4 - (offset & 3);
				// reads instruction
				this.readLabel(offset + this.readInt(u), labels);
				for (int i = this.readInt(u + 4); i > 0; --i)
				{
					this.readLabel(offset + this.readInt(u + 12), labels);
					u += 8;
				}
				u += 8;
				break;
			case ClassWriter.VAR_INSN:
			case ClassWriter.SBYTE_INSN:
			case ClassWriter.LDC_INSN:
				u += 2;
				break;
			case ClassWriter.SHORT_INSN:
			case ClassWriter.LDCW_INSN:
			case ClassWriter.FIELDORMETH_INSN:
			case ClassWriter.TYPE_INSN:
			case ClassWriter.IINC_INSN:
				u += 3;
				break;
			case ClassWriter.ITFMETH_INSN:
			case ClassWriter.INDYMETH_INSN:
				u += 5;
				break;
			// case MANA_INSN:
			default:
				u += 4;
				break;
			}
		}

		// reads the try catch entries to find the labels, and also visits them
		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			Label start = this.readLabel(this.readUnsignedShort(u + 2), labels);
			Label end = this.readLabel(this.readUnsignedShort(u + 4), labels);
			Label handler = this.readLabel(this.readUnsignedShort(u + 6), labels);
			String type = this.readUTF8(this.items[this.readUnsignedShort(u + 8)], c);
			mv.visitTryCatchBlock(start, end, handler, type);
			u += 8;
		}
		u += 2;

		// reads the code attributes
		int[] tanns = null; // start index of each visible type annotation
		int[] itanns = null; // start index of each invisible type annotation
		int tann = 0; // current index in tanns array
		int itann = 0; // current index in itanns array
		int ntoff = -1; // next visible type annotation code offset
		int nitoff = -1; // next invisible type annotation code offset
		int varTable = 0;
		int varTypeTable = 0;
		boolean zip = true;
		boolean unzip = (context.flags & EXPAND_FRAMES) != 0;
		int stackMap = 0;
		int stackMapSize = 0;
		int frameCount = 0;
		Context frame = null;
		Attribute attributes = null;

		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			String attrName = this.readUTF8(u + 2, c);
			switch (attrName)
			{
			case "LocalVariableTable":
				if ((context.flags & SKIP_DEBUG) != 0)
				{
					break;
				}

				varTable = u + 8;
				for (int j = this.readUnsignedShort(u + 8), v = u; j > 0; --j)
				{
					int label = this.readUnsignedShort(v + 10);
					if (labels[label] == null)
					{
						this.readLabel(label, labels).status |= Label.DEBUG;
					}
					label += this.readUnsignedShort(v + 12);
					if (labels[label] == null)
					{
						this.readLabel(label, labels).status |= Label.DEBUG;
					}
					v += 10;
				}
				break;
			case "LocalVariableTypeTable":
				varTypeTable = u + 8;
				break;
			case "LineNumberTable":
				if ((context.flags & SKIP_DEBUG) != 0)
				{
					break;
				}

				for (int j = this.readUnsignedShort(u + 8), v = u; j > 0; --j)
				{
					int label = this.readUnsignedShort(v + 10);
					if (labels[label] == null)
					{
						this.readLabel(label, labels).status |= Label.DEBUG;
					}
					Label l = labels[label];
					while (l.line > 0)
					{
						if (l.next == null)
						{
							l.next = new Label();
						}
						l = l.next;
					}
					l.line = this.readUnsignedShort(v + 12);
					v += 4;
				}
				break;
			case "RuntimeVisibleTypeAnnotations":
				tanns = this.readTypeAnnotations(mv, context, u + 8, true);
				ntoff = tanns.length == 0 || this.readByte(tanns[0]) < 0x43 ? -1 : this.readUnsignedShort(tanns[0] + 1);
				break;
			case "RuntimeInvisibleTypeAnnotations":
				itanns = this.readTypeAnnotations(mv, context, u + 8, false);
				nitoff =
					itanns.length == 0 || this.readByte(itanns[0]) < 0x43 ? -1 : this.readUnsignedShort(itanns[0] + 1);
				break;
			case "StackMapTable":
				if ((context.flags & SKIP_FRAMES) == 0)
				{
					stackMap = u + 10;
					stackMapSize = this.readInt(u + 4);
					frameCount = this.readUnsignedShort(u + 8);
				}
				/*
				 * here we do not extract the labels corresponding to the
				 * attribute content. This would require a full parsing of the
				 * attribute, which would need to be repeated in the second
				 * phase (see below). Instead the content of the attribute is
				 * read one frame at a time (i.e. after a frame has been
				 * visited, the next frame is read), and the labels it contains
				 * are also extracted one frame at a time. Thanks to the
				 * ordering of frames, having only a "one frame lookahead" is
				 * not a problem, i.e. it is not possible to see an offset
				 * smaller than the offset of the current insn and for which no
				 * Label exist.
				 */
				/*
				 * This is not true for UNINITIALIZED type offsets. We solve
				 * this by parsing the stack map table without a full decoding
				 * (see below).
				 */
				break;
			case "StackMap":
				if ((context.flags & SKIP_FRAMES) == 0)
				{
					zip = false;
					stackMap = u + 10;
					stackMapSize = this.readInt(u + 4);
					frameCount = this.readUnsignedShort(u + 8);
				}
				/*
				 * IMPORTANT! here we assume that the frames are ordered, as in
				 * the StackMapTable attribute, although this is not guaranteed
				 * by the attribute format.
				 */
				break;
			default:
				for (Attribute attr2 : context.attrs)
				{
					if (!attr2.type.equals(attrName))
					{
						continue;
					}

					final Attribute attr = attr2.read(this, u + 8, this.readInt(u + 4), c, codeStart - 8, labels);
					if (attr != null)
					{
						attr.next = attributes;
						attributes = attr;
					}
				}
				break;
			}
			u += 6 + this.readInt(u + 4);
		}
		//noinspection UnusedAssignment
		u += 2;

		// generates the first (implicit) stack map frame
		if (stackMap != 0)
		{
			/*
			 * for the first explicit frame the offset is not offset_delta + 1
			 * but only offset_delta; setting the implicit frame offset to -1
			 * allow the use of the "offset_delta + 1" rule in all cases
			 */
			frame = context;
			frame.offset = -1;
			frame.mode = 0;
			frame.localCount = 0;
			frame.localDiff = 0;
			frame.stackCount = 0;
			frame.local = new Object[maxLocals];
			frame.stack = new Object[maxStack];
			if (unzip)
			{
				this.getImplicitFrame(context);
			}
			/*
			 * Finds labels for UNINITIALIZED frame types. Instead of decoding
			 * each element of the stack map table, we look for 3 consecutive
			 * bytes that "look like" an UNINITIALIZED type (tag 8, offset
			 * within code bounds, NEW instruction at this offset). We may find
			 * false positives (i.e. not real UNINITIALIZED types), but this
			 * should be rare, and the only consequence will be the creation of
			 * an unneeded label. This is better than creating a label for each
			 * NEW instruction, and faster than fully decoding the whole stack
			 * map table.
			 */
			for (int i = stackMap; i < stackMap + stackMapSize - 2; ++i)
			{
				if (b[i] != 8)
				{
					continue;
				}
				final int offset = this.readUnsignedShort(i + 1);
				if (offset < 0 || offset >= codeLength)
				{
					continue;
				}
				if ((b[codeStart + offset] & 0xFF) == ASMConstants.NEW)
				{
					this.readLabel(offset, labels);
				}
			}
		}

		// visits the instructions
		u = codeStart;
		while (u < codeEnd)
		{
			int offset = u - codeStart;

			// visits the label and line number for this offset, if any
			Label l = labels[offset];
			if (l != null)
			{
				Label next = l.next;
				l.next = null;
				mv.visitLabel(l);
				if ((context.flags & SKIP_DEBUG) == 0 && l.line > 0)
				{
					mv.visitLineNumber(l.line, l);
					while (next != null)
					{
						mv.visitLineNumber(next.line, l);
						next = next.next;
					}
				}
			}

			// visits the frame for this offset, if any
			while (frame != null && (frame.offset == offset || frame.offset == -1))
			{
				// if there is a frame for this offset, makes the visitor visit
				// it, and reads the next frame if there is one.
				if (frame.offset != -1)
				{
					if (!zip || unzip)
					{
						mv.visitFrame(ASMConstants.F_NEW, frame.localCount, frame.local, frame.stackCount, frame.stack);
					}
					else
					{
						mv.visitFrame(frame.mode, frame.localDiff, frame.local, frame.stackCount, frame.stack);
					}
				}
				if (frameCount > 0)
				{
					stackMap = this.readFrame(stackMap, zip, unzip, frame);
					--frameCount;
				}
				else
				{
					frame = null;
				}
			}

			// visits the instruction at this offset
			int opcode = b[u] & 0xFF;
			switch (ClassWriter.TYPE[opcode])
			{
			case ClassWriter.NOARG_INSN:
				mv.visitInsn(opcode);
				u += 1;
				break;
			case ClassWriter.IMPLVAR_INSN:
				if (opcode > ASMConstants.ISTORE)
				{
					opcode -= 59; // ISTORE_0
					mv.visitVarInsn(ASMConstants.ISTORE + (opcode >> 2), opcode & 0x3);
				}
				else
				{
					opcode -= 26; // ILOAD_0
					mv.visitVarInsn(ASMConstants.ILOAD + (opcode >> 2), opcode & 0x3);
				}
				u += 1;
				break;
			case ClassWriter.LABEL_INSN:
				mv.visitJumpInsn(opcode, labels[offset + this.readShort(u + 1)]);
				u += 3;
				break;
			case ClassWriter.LABELW_INSN:
				mv.visitJumpInsn(opcode - 33, labels[offset + this.readInt(u + 1)]);
				u += 5;
				break;
			case ClassWriter.WIDE_INSN:
				opcode = b[u + 1] & 0xFF;
				if (opcode == ASMConstants.IINC)
				{
					mv.visitIincInsn(this.readUnsignedShort(u + 2), this.readShort(u + 4));
					u += 6;
				}
				else
				{
					mv.visitVarInsn(opcode, this.readUnsignedShort(u + 2));
					u += 4;
				}
				break;
			case ClassWriter.TABL_INSN:
			{
				// skips 0 to 3 padding bytes
				u = u + 4 - (offset & 3);
				// reads instruction
				int label = offset + this.readInt(u);
				int min = this.readInt(u + 4);
				int max = this.readInt(u + 8);
				Label[] table = new Label[max - min + 1];
				u += 12;
				for (int i = 0; i < table.length; ++i)
				{
					table[i] = labels[offset + this.readInt(u)];
					u += 4;
				}
				mv.visitTableSwitchInsn(min, max, labels[label], table);
				break;
			}
			case ClassWriter.LOOK_INSN:
			{
				// skips 0 to 3 padding bytes
				u = u + 4 - (offset & 3);
				// reads instruction
				int label = offset + this.readInt(u);
				int len = this.readInt(u + 4);
				int[] keys = new int[len];
				Label[] values = new Label[len];
				u += 8;
				for (int i = 0; i < len; ++i)
				{
					keys[i] = this.readInt(u);
					values[i] = labels[offset + this.readInt(u + 4)];
					u += 8;
				}
				mv.visitLookupSwitchInsn(labels[label], keys, values);
				break;
			}
			case ClassWriter.VAR_INSN:
				mv.visitVarInsn(opcode, b[u + 1] & 0xFF);
				u += 2;
				break;
			case ClassWriter.SBYTE_INSN:
				mv.visitIntInsn(opcode, b[u + 1]);
				u += 2;
				break;
			case ClassWriter.SHORT_INSN:
				mv.visitIntInsn(opcode, this.readShort(u + 1));
				u += 3;
				break;
			case ClassWriter.LDC_INSN:
				mv.visitLdcInsn(this.readConst(b[u + 1] & 0xFF, c));
				u += 2;
				break;
			case ClassWriter.LDCW_INSN:
				mv.visitLdcInsn(this.readConst(this.readUnsignedShort(u + 1), c));
				u += 3;
				break;
			case ClassWriter.FIELDORMETH_INSN:
			case ClassWriter.ITFMETH_INSN:
			{
				int cpIndex = this.items[this.readUnsignedShort(u + 1)];
				boolean itf = b[cpIndex - 1] == ClassWriter.IMETH;
				String iowner = this.readClass(cpIndex, c);
				cpIndex = this.items[this.readUnsignedShort(cpIndex + 2)];
				String iname = this.readUTF8(cpIndex, c);
				String idesc = this.readUTF8(cpIndex + 2, c);
				if (opcode < ASMConstants.INVOKEVIRTUAL)
				{
					mv.visitFieldInsn(opcode, iowner, iname, idesc);
				}
				else
				{
					mv.visitMethodInsn(opcode, iowner, iname, idesc, itf);
				}
				if (opcode == ASMConstants.INVOKEINTERFACE)
				{
					u += 5;
				}
				else
				{
					u += 3;
				}
				break;
			}
			case ClassWriter.INDYMETH_INSN:
			{
				int cpIndex = this.items[this.readUnsignedShort(u + 1)];
				int bsmIndex = context.bootstrapMethods[this.readUnsignedShort(cpIndex)];
				Handle bsm = (Handle) this.readConst(this.readUnsignedShort(bsmIndex), c);
				int bsmArgCount = this.readUnsignedShort(bsmIndex + 2);
				Object[] bsmArgs = new Object[bsmArgCount];
				bsmIndex += 4;
				for (int i = 0; i < bsmArgCount; i++)
				{
					bsmArgs[i] = this.readConst(this.readUnsignedShort(bsmIndex), c);
					bsmIndex += 2;
				}
				cpIndex = this.items[this.readUnsignedShort(cpIndex + 2)];
				String iname = this.readUTF8(cpIndex, c);
				String idesc = this.readUTF8(cpIndex + 2, c);
				mv.visitInvokeDynamicInsn(iname, idesc, bsm, bsmArgs);
				u += 5;
				break;
			}
			case ClassWriter.TYPE_INSN:
				mv.visitTypeInsn(opcode, this.readClass(u + 1, c));
				u += 3;
				break;
			case ClassWriter.IINC_INSN:
				mv.visitIincInsn(b[u + 1] & 0xFF, b[u + 2]);
				u += 3;
				break;
			// case MANA_INSN:
			default:
				mv.visitMultiANewArrayInsn(this.readClass(u + 1, c), b[u + 3] & 0xFF);
				u += 4;
				break;
			}

			// visit the instruction annotations, if any
			while (tanns != null && tann < tanns.length && ntoff <= offset)
			{
				if (ntoff == offset)
				{
					int v = this.readAnnotationTarget(context, tanns[tann]);
					this.readAnnotationValues(v + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath,
					                                                                 this.readUTF8(v, c), true));
				}
				ntoff = ++tann >= tanns.length || this.readByte(tanns[tann]) < 0x43 ?
					        -1 :
					        this.readUnsignedShort(tanns[tann] + 1);
			}
			while (itanns != null && itann < itanns.length && nitoff <= offset)
			{
				if (nitoff == offset)
				{
					int v = this.readAnnotationTarget(context, itanns[itann]);
					this.readAnnotationValues(v + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath,
					                                                                 this.readUTF8(v, c), false));
				}
				nitoff = ++itann >= itanns.length || this.readByte(itanns[itann]) < 0x43 ?
					         -1 :
					         this.readUnsignedShort(itanns[itann] + 1);
			}
		}
		if (labels[codeLength] != null)
		{
			mv.visitLabel(labels[codeLength]);
		}

		// visits the local variable tables
		if ((context.flags & SKIP_DEBUG) == 0 && varTable != 0)
		{
			int[] typeTable = null;
			if (varTypeTable != 0)
			{
				u = varTypeTable + 2;
				typeTable = new int[this.readUnsignedShort(varTypeTable) * 3];
				for (int i = typeTable.length; i > 0; )
				{
					typeTable[--i] = u + 6; // signature
					typeTable[--i] = this.readUnsignedShort(u + 8); // index
					typeTable[--i] = this.readUnsignedShort(u); // start
					u += 10;
				}
			}
			u = varTable + 2;
			for (int i = this.readUnsignedShort(varTable); i > 0; --i)
			{
				int start = this.readUnsignedShort(u);
				int length = this.readUnsignedShort(u + 2);
				int index = this.readUnsignedShort(u + 8);
				String vsignature = null;
				if (typeTable != null)
				{
					for (int j = 0; j < typeTable.length; j += 3)
					{
						if (typeTable[j] == start && typeTable[j + 1] == index)
						{
							vsignature = this.readUTF8(typeTable[j + 2], c);
							break;
						}
					}
				}
				mv.visitLocalVariable(this.readUTF8(u + 4, c), this.readUTF8(u + 6, c), vsignature, labels[start],
				                      labels[start + length], index);
				u += 10;
			}
		}

		// visits the local variables type annotations
		if (tanns != null)
		{
			for (int tann2 : tanns)
			{
				if (this.readByte(tann2) >> 1 != 0x40 >> 1)
				{
					continue;
				}
				final int v = this.readAnnotationTarget(context, tann2);
				this.readAnnotationValues(v + 2, c, true,
				                          mv.visitLocalVariableAnnotation(context.typeRef, context.typePath,
				                                                          context.start, context.end, context.index,
				                                                          this.readUTF8(v, c), true));
			}
		}
		if (itanns != null)
		{
			for (int itann2 : itanns)
			{
				if (this.readByte(itann2) >> 1 != 0x40 >> 1)
				{
					continue;
				}
				final int v = this.readAnnotationTarget(context, itann2);
				this.readAnnotationValues(v + 2, c, true,
				                          mv.visitLocalVariableAnnotation(context.typeRef, context.typePath,
				                                                          context.start, context.end, context.index,
				                                                          this.readUTF8(v, c), false));
			}
		}

		// visits the code attributes
		while (attributes != null)
		{
			final Attribute attr = attributes.next;
			attributes.next = null;
			mv.visitAttribute(attributes);
			attributes = attr;
		}

		// visits the max stack and max locals values
		mv.visitMaxs(maxStack, maxLocals);
	}

	private int[] readTypeAnnotations(final MethodVisitor mv, final Context context, int u, boolean visible)
	{
		char[] c = context.buffer;
		int[] offsets = new int[this.readUnsignedShort(u)];
		u += 2;
		for (int i = 0; i < offsets.length; ++i)
		{
			offsets[i] = u;
			int target = this.readInt(u);
			switch (target >>> 24)
			{
			case 0x00: // CLASS_TYPE_PARAMETER
			case 0x01: // METHOD_TYPE_PARAMETER
			case 0x16: // METHOD_FORMAL_PARAMETER
				u += 2;
				break;
			case 0x13: // FIELD
			case 0x14: // METHOD_RETURN
			case 0x15: // METHOD_RECEIVER
				u += 1;
				break;
			case 0x40: // LOCAL_VARIABLE
			case 0x41: // RESOURCE_VARIABLE
				for (int j = this.readUnsignedShort(u + 1); j > 0; --j)
				{
					int start = this.readUnsignedShort(u + 3);
					int length = this.readUnsignedShort(u + 5);
					this.readLabel(start, context.labels);
					this.readLabel(start + length, context.labels);
					u += 6;
				}
				u += 3;
				break;
			case 0x47: // CAST
			case 0x48: // CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
			case 0x49: // METHOD_INVOCATION_TYPE_ARGUMENT
			case 0x4A: // CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
			case 0x4B: // METHOD_REFERENCE_TYPE_ARGUMENT
				u += 4;
				break;
			// case 0x10: // CLASS_EXTENDS
			// case 0x11: // CLASS_TYPE_PARAMETER_BOUND
			// case 0x12: // METHOD_TYPE_PARAMETER_BOUND
			// case 0x17: // THROWS
			// case 0x42: // EXCEPTION_PARAMETER
			// case 0x43: // INSTANCEOF
			// case 0x44: // NEW
			// case 0x45: // CONSTRUCTOR_REFERENCE
			// case 0x46: // METHOD_REFERENCE
			default:
				u += 3;
				break;
			}
			int pathLength = this.readByte(u);
			if (target >>> 24 == 0x42)
			{
				TypePath path = pathLength == 0 ? null : new TypePath(this.b, u);
				u += 1 + 2 * pathLength;
				u = this.readAnnotationValues(u + 2, c, true,
				                              mv.visitTryCatchAnnotation(target, path, this.readUTF8(u, c), visible));
			}
			else
			{
				u = this.readAnnotationValues(u + 3 + 2 * pathLength, c, true, null);
			}
		}
		return offsets;
	}

	private int readAnnotationTarget(final Context context, int u)
	{
		int target = this.readInt(u);
		switch (target >>> 24)
		{
		case 0x00: // CLASS_TYPE_PARAMETER
		case 0x01: // METHOD_TYPE_PARAMETER
		case 0x16: // METHOD_FORMAL_PARAMETER
			target &= 0xFFFF0000;
			u += 2;
			break;
		case 0x13: // FIELD
		case 0x14: // METHOD_RETURN
		case 0x15: // METHOD_RECEIVER
			target &= 0xFF000000;
			u += 1;
			break;
		case 0x40: // LOCAL_VARIABLE
		case 0x41:
		{ // RESOURCE_VARIABLE
			target &= 0xFF000000;
			int n = this.readUnsignedShort(u + 1);
			context.start = new Label[n];
			context.end = new Label[n];
			context.index = new int[n];
			u += 3;
			for (int i = 0; i < n; ++i)
			{
				int start = this.readUnsignedShort(u);
				int length = this.readUnsignedShort(u + 2);
				context.start[i] = this.readLabel(start, context.labels);
				context.end[i] = this.readLabel(start + length, context.labels);
				context.index[i] = this.readUnsignedShort(u + 4);
				u += 6;
			}
			break;
		}
		case 0x47: // CAST
		case 0x48: // CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
		case 0x49: // METHOD_INVOCATION_TYPE_ARGUMENT
		case 0x4A: // CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
		case 0x4B: // METHOD_REFERENCE_TYPE_ARGUMENT
			target &= 0xFF0000FF;
			u += 4;
			break;
		// case 0x10: // CLASS_EXTENDS
		// case 0x11: // CLASS_TYPE_PARAMETER_BOUND
		// case 0x12: // METHOD_TYPE_PARAMETER_BOUND
		// case 0x17: // THROWS
		// case 0x42: // EXCEPTION_PARAMETER
		// case 0x43: // INSTANCEOF
		// case 0x44: // NEW
		// case 0x45: // CONSTRUCTOR_REFERENCE
		// case 0x46: // METHOD_REFERENCE
		default:
			target &= target >>> 24 < 0x43 ? 0xFFFFFF00 : 0xFF000000;
			u += 3;
			break;
		}
		int pathLength = this.readByte(u);
		context.typeRef = target;
		context.typePath = pathLength == 0 ? null : new TypePath(this.b, u);
		return u + 1 + 2 * pathLength;
	}

	private void readParameterAnnotations(final MethodVisitor mv, final Context context, int v, final boolean visible)
	{
		int i;
		int n = this.b[v++] & 0xFF;
		// workaround for a bug in javac (javac compiler generates a parameter
		// annotation array whose size is equal to the number of parameters in
		// the Java source file, while it should generate an array whose size is
		// equal to the number of parameters in the method descriptor - which
		// includes the synthetic parameters added by the compiler). This work-
		// around supposes that the synthetic parameters are the first ones.
		int synthetics = Type.getArgumentTypes(context.desc).length - n;
		AnnotationVisitor av;
		for (i = 0; i < synthetics; ++i)
		{
			// virtual annotation to detect synthetic parameters in MethodWriter
			av = mv.visitParameterAnnotation(i, "Ljava/lang/Synthetic;", false);
			if (av != null)
			{
				av.visitEnd();
			}
		}
		char[] c = context.buffer;
		for (; i < n + synthetics; ++i)
		{
			int j = this.readUnsignedShort(v);
			v += 2;
			for (; j > 0; --j)
			{
				av = mv.visitParameterAnnotation(i, this.readUTF8(v, c), visible);
				v = this.readAnnotationValues(v + 2, c, true, av);
			}
		}
	}

	private int readAnnotationValues(int v, final char[] buf, final boolean named, final AnnotationVisitor av)
	{
		int i = this.readUnsignedShort(v);
		v += 2;
		if (named)
		{
			for (; i > 0; --i)
			{
				v = this.readAnnotationValue(v + 2, buf, this.readUTF8(v, buf), av);
			}
		}
		else
		{
			for (; i > 0; --i)
			{
				v = this.readAnnotationValue(v, buf, null, av);
			}
		}
		if (av != null)
		{
			av.visitEnd();
		}
		return v;
	}

	private int readAnnotationValue(int v, final char[] buf, final String name, final AnnotationVisitor av)
	{
		int i;
		if (av == null)
		{
			switch (this.b[v] & 0xFF)
			{
			case 'e': // enum_const_value
				return v + 5;
			case '@': // annotation_value
				return this.readAnnotationValues(v + 3, buf, true, null);
			case '[': // array_value
				return this.readAnnotationValues(v + 1, buf, false, null);
			default:
				return v + 3;
			}
		}
		switch (this.b[v++] & 0xFF)
		{
		case 'I': // pointer to CONSTANT_Integer
		case 'J': // pointer to CONSTANT_Long
		case 'F': // pointer to CONSTANT_Float
		case 'D': // pointer to CONSTANT_Double
			av.visit(name, this.readConst(this.readUnsignedShort(v), buf));
			v += 2;
			break;
		case 'B': // pointer to CONSTANT_Byte
			av.visit(name, (byte) this.readInt(this.items[this.readUnsignedShort(v)]));
			v += 2;
			break;
		case 'Z': // pointer to CONSTANT_Boolean
			av.visit(name, this.readInt(this.items[this.readUnsignedShort(v)]) == 0 ? Boolean.FALSE : Boolean.TRUE);
			v += 2;
			break;
		case 'S': // pointer to CONSTANT_Short
			av.visit(name, (short) this.readInt(this.items[this.readUnsignedShort(v)]));
			v += 2;
			break;
		case 'C': // pointer to CONSTANT_Char
			av.visit(name, (char) this.readInt(this.items[this.readUnsignedShort(v)]));
			v += 2;
			break;
		case 's': // pointer to CONSTANT_Utf8
			av.visit(name, this.readUTF8(v, buf));
			v += 2;
			break;
		case 'e': // enum_const_value
			av.visitEnum(name, this.readUTF8(v, buf), this.readUTF8(v + 2, buf));
			v += 4;
			break;
		case 'c': // class_info
			av.visit(name, Type.getType(this.readUTF8(v, buf)));
			v += 2;
			break;
		case '@': // annotation_value
			v = this.readAnnotationValues(v + 2, buf, true, av.visitAnnotation(name, this.readUTF8(v, buf)));
			break;
		case '[': // array_value
			int size = this.readUnsignedShort(v);
			v += 2;
			if (size == 0)
			{
				return this.readAnnotationValues(v - 2, buf, false, av.visitArray(name));
			}
			switch (this.b[v++] & 0xFF)
			{
			case 'B':
				byte[] bv = new byte[size];
				for (i = 0; i < size; i++)
				{
					bv[i] = (byte) this.readInt(this.items[this.readUnsignedShort(v)]);
					v += 3;
				}
				av.visit(name, bv);
				--v;
				break;
			case 'Z':
				boolean[] zv = new boolean[size];
				for (i = 0; i < size; i++)
				{
					zv[i] = this.readInt(this.items[this.readUnsignedShort(v)]) != 0;
					v += 3;
				}
				av.visit(name, zv);
				--v;
				break;
			case 'S':
				short[] sv = new short[size];
				for (i = 0; i < size; i++)
				{
					sv[i] = (short) this.readInt(this.items[this.readUnsignedShort(v)]);
					v += 3;
				}
				av.visit(name, sv);
				--v;
				break;
			case 'C':
				char[] cv = new char[size];
				for (i = 0; i < size; i++)
				{
					cv[i] = (char) this.readInt(this.items[this.readUnsignedShort(v)]);
					v += 3;
				}
				av.visit(name, cv);
				--v;
				break;
			case 'I':
				int[] iv = new int[size];
				for (i = 0; i < size; i++)
				{
					iv[i] = this.readInt(this.items[this.readUnsignedShort(v)]);
					v += 3;
				}
				av.visit(name, iv);
				--v;
				break;
			case 'J':
				long[] lv = new long[size];
				for (i = 0; i < size; i++)
				{
					lv[i] = this.readLong(this.items[this.readUnsignedShort(v)]);
					v += 3;
				}
				av.visit(name, lv);
				--v;
				break;
			case 'F':
				float[] fv = new float[size];
				for (i = 0; i < size; i++)
				{
					fv[i] = Float.intBitsToFloat(this.readInt(this.items[this.readUnsignedShort(v)]));
					v += 3;
				}
				av.visit(name, fv);
				--v;
				break;
			case 'D':
				double[] dv = new double[size];
				for (i = 0; i < size; i++)
				{
					dv[i] = Double.longBitsToDouble(this.readLong(this.items[this.readUnsignedShort(v)]));
					v += 3;
				}
				av.visit(name, dv);
				--v;
				break;
			default:
				v = this.readAnnotationValues(v - 3, buf, false, av.visitArray(name));
			}
		}
		return v;
	}

	private void getImplicitFrame(final Context frame)
	{
		String desc = frame.desc;
		Object[] locals = frame.local;
		int local = 0;
		if ((frame.access & ASMConstants.ACC_STATIC) == 0)
		{
			if ("<init>".equals(frame.name))
			{
				locals[local++] = ASMConstants.UNINITIALIZED_THIS;
			}
			else
			{
				locals[local++] = this.readClass(this.header + 2, frame.buffer);
			}
		}
		int i = 1;
		loop:
		while (true)
		{
			int j = i;
			switch (desc.charAt(i++))
			{
			case 'Z':
			case 'C':
			case 'B':
			case 'S':
			case 'I':
				locals[local++] = ASMConstants.INTEGER;
				break;
			case 'F':
				locals[local++] = ASMConstants.FLOAT;
				break;
			case 'J':
				locals[local++] = ASMConstants.LONG;
				break;
			case 'D':
				locals[local++] = ASMConstants.DOUBLE;
				break;
			case '[':
				while (desc.charAt(i) == '[')
				{
					++i;
				}
				if (desc.charAt(i) == 'L')
				{
					++i;
					while (desc.charAt(i) != ';')
					{
						++i;
					}
				}
				locals[local++] = desc.substring(j, ++i);
				break;
			case 'L':
				while (desc.charAt(i) != ';')
				{
					++i;
				}
				locals[local++] = desc.substring(j + 1, i++);
				break;
			default:
				break loop;
			}
		}
		frame.localCount = local;
	}

	private int readFrame(int stackMap, boolean zip, boolean unzip, Context frame)
	{
		char[] c = frame.buffer;
		Label[] labels = frame.labels;
		int tag;
		int delta;
		if (zip)
		{
			tag = this.b[stackMap++] & 0xFF;
		}
		else
		{
			tag = MethodWriter.FULL_FRAME;
			frame.offset = -1;
		}
		frame.localDiff = 0;
		if (tag < MethodWriter.SAME_LOCALS_1_STACK_ITEM_FRAME)
		{
			delta = tag;
			frame.mode = ASMConstants.F_SAME;
			frame.stackCount = 0;
		}
		else if (tag < MethodWriter.RESERVED)
		{
			delta = tag - MethodWriter.SAME_LOCALS_1_STACK_ITEM_FRAME;
			stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
			frame.mode = ASMConstants.F_SAME1;
			frame.stackCount = 1;
		}
		else
		{
			delta = this.readUnsignedShort(stackMap);
			stackMap += 2;
			if (tag == MethodWriter.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
			{
				stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
				frame.mode = ASMConstants.F_SAME1;
				frame.stackCount = 1;
			}
			else if (tag >= MethodWriter.CHOP_FRAME && tag < MethodWriter.SAME_FRAME_EXTENDED)
			{
				frame.mode = ASMConstants.F_CHOP;
				frame.localDiff = MethodWriter.SAME_FRAME_EXTENDED - tag;
				frame.localCount -= frame.localDiff;
				frame.stackCount = 0;
			}
			else if (tag == MethodWriter.SAME_FRAME_EXTENDED)
			{
				frame.mode = ASMConstants.F_SAME;
				frame.stackCount = 0;
			}
			else if (tag < MethodWriter.FULL_FRAME)
			{
				int local = unzip ? frame.localCount : 0;
				for (int i = tag - MethodWriter.SAME_FRAME_EXTENDED; i > 0; i--)
				{
					stackMap = this.readFrameType(frame.local, local++, stackMap, c, labels);
				}
				frame.mode = ASMConstants.F_APPEND;
				frame.localDiff = tag - MethodWriter.SAME_FRAME_EXTENDED;
				frame.localCount += frame.localDiff;
				frame.stackCount = 0;
			}
			else
			{ // if (tag == FULL_FRAME) {
				frame.mode = ASMConstants.F_FULL;
				int n = this.readUnsignedShort(stackMap);
				stackMap += 2;
				frame.localDiff = n;
				frame.localCount = n;
				for (int local = 0; n > 0; n--)
				{
					stackMap = this.readFrameType(frame.local, local++, stackMap, c, labels);
				}
				n = this.readUnsignedShort(stackMap);
				stackMap += 2;
				frame.stackCount = n;
				for (int stack = 0; n > 0; n--)
				{
					stackMap = this.readFrameType(frame.stack, stack++, stackMap, c, labels);
				}
			}
		}
		frame.offset += delta + 1;
		this.readLabel(frame.offset, labels);
		return stackMap;
	}

	private int readFrameType(final Object[] frame, final int index, int v, final char[] buf, final Label[] labels)
	{
		int type = this.b[v++] & 0xFF;
		switch (type)
		{
		case 0:
			frame[index] = ASMConstants.TOP;
			break;
		case 1:
			frame[index] = ASMConstants.INTEGER;
			break;
		case 2:
			frame[index] = ASMConstants.FLOAT;
			break;
		case 3:
			frame[index] = ASMConstants.DOUBLE;
			break;
		case 4:
			frame[index] = ASMConstants.LONG;
			break;
		case 5:
			frame[index] = ASMConstants.NULL;
			break;
		case 6:
			frame[index] = ASMConstants.UNINITIALIZED_THIS;
			break;
		case 7: // Object
			frame[index] = this.readClass(v, buf);
			v += 2;
			break;
		default: // Uninitialized
			frame[index] = this.readLabel(this.readUnsignedShort(v), labels);
			v += 2;
		}
		return v;
	}

	protected Label readLabel(int offset, Label[] labels)
	{
		if (labels[offset] == null)
		{
			labels[offset] = new Label();
		}
		return labels[offset];
	}

	private int getAttributes()
	{
		// skips the header
		int u = this.header + 8 + this.readUnsignedShort(this.header + 6) * 2;
		// skips fields and methods
		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			for (int j = this.readUnsignedShort(u + 8); j > 0; --j)
			{
				u += 6 + this.readInt(u + 12);
			}
			u += 8;
		}
		u += 2;
		for (int i = this.readUnsignedShort(u); i > 0; --i)
		{
			for (int j = this.readUnsignedShort(u + 8); j > 0; --j)
			{
				u += 6 + this.readInt(u + 12);
			}
			u += 8;
		}
		// the attribute_info structure starts just after the methods
		return u + 2;
	}

	private Attribute readAttribute(final Attribute[] attrs, final String type, final int off, final int len,
		                               final char[] buf, final int codeOff, final Label[] labels)
	{
		for (Attribute attr : attrs)
		{
			if (attr.type.equals(type))
			{
				return attr.read(this, off, len, buf, codeOff, labels);
			}
		}
		return new Attribute(type).read(this, off, len, null, -1, null);
	}

	public int getItemCount()
	{
		return this.items.length;
	}

	public int getItem(final int item)
	{
		return this.items[item];
	}

	public int getMaxStringLength()
	{
		return this.maxStringLength;
	}

	public int readByte(final int index)
	{
		return this.b[index] & 0xFF;
	}

	public int readUnsignedShort(final int index)
	{
		byte[] b = this.b;
		return (b[index] & 0xFF) << 8 | b[index + 1] & 0xFF;
	}

	public short readShort(final int index)
	{
		byte[] b = this.b;
		return (short) ((b[index] & 0xFF) << 8 | b[index + 1] & 0xFF);
	}

	public int readInt(final int index)
	{
		byte[] b = this.b;
		return (b[index] & 0xFF) << 24 | (b[index + 1] & 0xFF) << 16 | (b[index + 2] & 0xFF) << 8 | b[index + 3] & 0xFF;
	}

	public long readLong(final int index)
	{
		long l1 = this.readInt(index);
		long l0 = this.readInt(index + 4) & 0xFFFFFFFFL;
		return l1 << 32 | l0;
	}

	public String readUTF8(int index, final char[] buf)
	{
		int item = this.readUnsignedShort(index);
		if (index == 0 || item == 0)
		{
			return null;
		}
		String s = this.strings[item];
		if (s != null)
		{
			return s;
		}
		index = this.items[item];
		return this.strings[item] = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
	}

	private String readUTF(int index, final int utfLen, final char[] buf)
	{
		int endIndex = index + utfLen;
		byte[] b = this.b;
		int strLen = 0;
		int c;
		int st = 0;
		char cc = 0;
		while (index < endIndex)
		{
			c = b[index++];
			switch (st)
			{
			case 0:
				c = c & 0xFF;
				if (c < 0x80)
				{ // 0xxxxxxx
					buf[strLen++] = (char) c;
				}
				else if (c < 0xE0 && c > 0xBF)
				{ // 110x xxxx 10xx xxxx
					cc = (char) (c & 0x1F);
					st = 1;
				}
				else
				{ // 1110 xxxx 10xx xxxx 10xx xxxx
					cc = (char) (c & 0x0F);
					st = 2;
				}
				break;

			case 1: // byte 2 of 2-byte char or byte 3 of 3-byte char
				buf[strLen++] = (char) (cc << 6 | c & 0x3F);
				st = 0;
				break;

			case 2: // byte 2 of 3-byte char
				cc = (char) (cc << 6 | c & 0x3F);
				st = 1;
				break;
			}
		}
		return new String(buf, 0, strLen);
	}

	public String readClass(final int index, final char[] buf)
	{
		// computes the start index of the CONSTANT_Class item in b
		// and reads the CONSTANT_Utf8 item designated by
		// the first two bytes of this CONSTANT_Class item
		return this.readUTF8(this.items[this.readUnsignedShort(index)], buf);
	}

	public Object readConst(final int item, final char[] buf)
	{
		int index = this.items[item];
		switch (this.b[index - 1])
		{
		case ClassWriter.INT:
			return this.readInt(index);
		case ClassWriter.FLOAT:
			return Float.intBitsToFloat(this.readInt(index));
		case ClassWriter.LONG:
			return this.readLong(index);
		case ClassWriter.DOUBLE:
			return Double.longBitsToDouble(this.readLong(index));
		case ClassWriter.CLASS:
			return Type.getObjectType(this.readUTF8(index, buf));
		case ClassWriter.STR:
			return this.readUTF8(index, buf);
		case ClassWriter.MTYPE:
			return Type.getMethodType(this.readUTF8(index, buf));
		default: // case ClassWriter.HANDLE_BASE + [1..9]:
			int tag = this.readByte(index);
			int[] items = this.items;
			int cpIndex = items[this.readUnsignedShort(index + 1)];
			String owner = this.readClass(cpIndex, buf);
			cpIndex = items[this.readUnsignedShort(cpIndex + 2)];
			String name = this.readUTF8(cpIndex, buf);
			String desc = this.readUTF8(cpIndex + 2, buf);
			return new Handle(tag, owner, name, desc);
		}
	}
}
