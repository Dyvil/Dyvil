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

public class ClassWriter implements ClassVisitor
{
	public static final int COMPUTE_MAXS            = 1;
	public static final int COMPUTE_FRAMES          = 2;
	static final        int ACC_SYNTHETIC_ATTRIBUTE = 0x40000;
	static final        int TO_ACC_SYNTHETIC        = ACC_SYNTHETIC_ATTRIBUTE / Opcodes.ACC_SYNTHETIC;
	static final        int NOARG_INSN              = 0;
	static final        int SBYTE_INSN              = 1;
	static final        int SHORT_INSN              = 2;
	static final        int VAR_INSN                = 3;
	static final        int IMPLVAR_INSN            = 4;
	static final        int TYPE_INSN               = 5;
	static final        int FIELDORMETH_INSN        = 6;
	static final        int ITFMETH_INSN            = 7;
	static final        int INDYMETH_INSN           = 8;
	static final        int LABEL_INSN              = 9;
	static final        int LABELW_INSN             = 10;
	static final        int LDC_INSN                = 11;
	static final        int LDCW_INSN               = 12;
	static final        int IINC_INSN               = 13;
	static final        int TABL_INSN               = 14;
	static final        int LOOK_INSN               = 15;
	static final        int MANA_INSN               = 16;
	static final        int WIDE_INSN               = 17;
	
	static final byte[] TYPE;
	static final int CLASS     = 7;
	static final int FIELD     = 9;
	static final int METH      = 10;
	static final int IMETH     = 11;
	static final int STR       = 8;
	static final int INT       = 3;
	static final int FLOAT     = 4;
	static final int LONG      = 5;
	static final int DOUBLE    = 6;
	static final int NAME_TYPE = 12;
	static final int UTF8      = 1;
	static final int MTYPE     = 16;
	static final int HANDLE    = 15;
	static final int INDY      = 18;
	
	static final int HANDLE_BASE = 20;
	static final int TYPE_NORMAL = 30;
	static final int TYPE_UNINIT = 31;
	static final int TYPE_MERGED = 32;
	static final int BSM         = 33;
	
	ClassReader cr;
	int         version;
	int         index;
	final ByteVector pool;
	Item[] items;
	int    threshold;
	
	final Item key;
	final Item key2;
	final Item key3;
	final Item key4;
	
	Item[] typeTable;
	private short typeCount;
	
	private int access;
	private int name;
	String thisName;
	private int              signature;
	private int              superName;
	private int              interfaceCount;
	private int[]            interfaces;
	private int              sourceFile;
	private ByteVector       sourceDebug;
	private int              enclosingMethodOwner;
	private int              enclosingMethod;
	private AnnotationWriter anns;
	private AnnotationWriter ianns;
	private AnnotationWriter tanns;
	private AnnotationWriter itanns;
	private Attribute        attrs;
	private int              innerClassesCount;
	private ByteVector       innerClasses;
	int          bootstrapMethodsCount;
	ByteVector   bootstrapMethods;
	FieldWriter  firstField;
	FieldWriter  lastField;
	MethodWriter firstMethod;
	MethodWriter lastMethod;
	private boolean computeMaxs;
	private boolean computeFrames;
	boolean invalidFrames;
	
	static
	{
		int i;
		byte[] b = new byte[220];
		String s = "AAAAAAAAAAAAAAAABCLMMDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADD"
				+ "DDDEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
				+ "AAAAAAAAAAAAAAAAANAAAAAAAAAAAAAAAAAAAAJJJJJJJJJJJJJJJJDOPAA"
				+ "AAAAGGGGGGGHIFBFAAFFAARQJJKKJJJJJJJJJJJJJJJJJJ";
		for (i = 0; i < b.length; ++i)
		{
			b[i] = (byte) (s.charAt(i) - 'A');
		}
		TYPE = b;
	}
	
	public ClassWriter(final int flags)
	{
		this.index = 1;
		this.pool = new ByteVector();
		this.items = new Item[256];
		this.threshold = (int) (0.75d * this.items.length);
		this.key = new Item();
		this.key2 = new Item();
		this.key3 = new Item();
		this.key4 = new Item();
		this.computeMaxs = (flags & COMPUTE_MAXS) != 0;
		this.computeFrames = (flags & COMPUTE_FRAMES) != 0;
	}
	
	public ClassWriter(final ClassReader classReader, final int flags)
	{
		this(flags);
		classReader.copyPool(this);
		this.cr = classReader;
	}
	
	@Override
	public final void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces)
	{
		this.version = version;
		this.access = access;
		this.name = this.newClass(name);
		this.thisName = name;
		if (ClassReader.SIGNATURES && signature != null)
		{
			this.signature = this.newUTF8(signature);
		}
		this.superName = superName == null ? 0 : this.newClass(superName);
		if (interfaces != null && interfaces.length > 0)
		{
			this.interfaceCount = interfaces.length;
			this.interfaces = new int[this.interfaceCount];
			for (int i = 0; i < this.interfaceCount; ++i)
			{
				this.interfaces[i] = this.newClass(interfaces[i]);
			}
		}
	}
	
	@Override
	public final void visitSource(final String file, final String debug)
	{
		if (file != null)
		{
			this.sourceFile = this.newUTF8(file);
		}
		if (debug != null)
		{
			this.sourceDebug = new ByteVector().encodeUTF8(debug, 0, Integer.MAX_VALUE);
		}
	}
	
	@Override
	public final void visitOuterClass(final String owner, final String name, final String desc)
	{
		this.enclosingMethodOwner = this.newClass(owner);
		if (name != null && desc != null)
		{
			this.enclosingMethod = this.newNameType(name, desc);
		}
	}
	
	@Override
	public final AnnotationVisitor visitAnnotation(final String desc, final boolean visible)
	{
		if (!ClassReader.ANNOTATIONS)
		{
			return null;
		}
		ByteVector bv = new ByteVector();
		// write type, and reserve space for values count
		bv.putShort(this.newUTF8(desc)).putShort(0);
		AnnotationWriter aw = new AnnotationWriter(this, true, bv, bv, 2);
		if (visible)
		{
			aw.next = this.anns;
			this.anns = aw;
		}
		else
		{
			aw.next = this.ianns;
			this.ianns = aw;
		}
		return aw;
	}
	
	@Override
	public final AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, final String desc, final boolean visible)
	{
		if (!ClassReader.ANNOTATIONS)
		{
			return null;
		}
		ByteVector bv = new ByteVector();
		// write target_type and target_info
		AnnotationWriter.putTarget(typeRef, typePath, bv);
		// write type, and reserve space for values count
		bv.putShort(this.newUTF8(desc)).putShort(0);
		AnnotationWriter aw = new AnnotationWriter(this, true, bv, bv, bv.length - 2);
		if (visible)
		{
			aw.next = this.tanns;
			this.tanns = aw;
		}
		else
		{
			aw.next = this.itanns;
			this.itanns = aw;
		}
		return aw;
	}
	
	@Override
	public final void visitAttribute(final Attribute attr)
	{
		attr.next = this.attrs;
		this.attrs = attr;
	}
	
	@Override
	public final void visitInnerClass(final String name, final String outerName, final String innerName, final int access)
	{
		if (this.innerClasses == null)
		{
			this.innerClasses = new ByteVector();
		}
		Item nameItem = this.newClassItem(name);
		if (nameItem.intVal == 0)
		{
			++this.innerClassesCount;
			this.innerClasses.putShort(nameItem.index);
			this.innerClasses.putShort(outerName == null ? 0 : this.newClass(outerName));
			this.innerClasses.putShort(innerName == null ? 0 : this.newUTF8(innerName));
			this.innerClasses.putShort(access);
			nameItem.intVal = this.innerClassesCount;
		}
	}
	
	@Override
	public final FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value)
	{
		return new FieldWriter(this, access, name, desc, signature, value);
	}
	
	@Override
	public final MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions)
	{
		return new MethodWriter(this, access, name, desc, signature, exceptions, this.computeMaxs, this.computeFrames);
	}
	
	@Override
	public final void visitEnd()
	{
	}
	
	public byte[] toByteArray()
	{
		if (this.index > 0xFFFF)
		{
			throw new RuntimeException("Class file too large!");
		}
		// computes the real size of the bytecode of this class
		int size = 24 + 2 * this.interfaceCount;
		int nbFields = 0;
		FieldWriter fb = this.firstField;
		while (fb != null)
		{
			++nbFields;
			size += fb.getSize();
			fb = fb.next;
		}
		int nbMethods = 0;
		MethodWriter mb = this.firstMethod;
		while (mb != null)
		{
			++nbMethods;
			size += mb.getSize();
			mb = mb.next;
		}
		int attributeCount = 0;
		if (this.bootstrapMethods != null)
		{
			// we put it as first attribute in order to improve a bit
			// ClassReader.copyBootstrapMethods
			++attributeCount;
			size += 8 + this.bootstrapMethods.length;
			this.newUTF8("BootstrapMethods");
		}
		if (ClassReader.SIGNATURES && this.signature != 0)
		{
			++attributeCount;
			size += 8;
			this.newUTF8("Signature");
		}
		if (this.sourceFile != 0)
		{
			++attributeCount;
			size += 8;
			this.newUTF8("SourceFile");
		}
		if (this.sourceDebug != null)
		{
			++attributeCount;
			size += this.sourceDebug.length + 6;
			this.newUTF8("SourceDebugExtension");
		}
		if (this.enclosingMethodOwner != 0)
		{
			++attributeCount;
			size += 10;
			this.newUTF8("EnclosingMethod");
		}
		if ((this.access & Opcodes.ACC_DEPRECATED) != 0)
		{
			++attributeCount;
			size += 6;
			this.newUTF8("Deprecated");
		}
		if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if ((this.version & 0xFFFF) < Opcodes.V1_5 || (this.access & ACC_SYNTHETIC_ATTRIBUTE) != 0)
			{
				++attributeCount;
				size += 6;
				this.newUTF8("Synthetic");
			}
		}
		if (this.innerClasses != null)
		{
			++attributeCount;
			size += 8 + this.innerClasses.length;
			this.newUTF8("InnerClasses");
		}
		if (ClassReader.ANNOTATIONS && this.anns != null)
		{
			++attributeCount;
			size += 8 + this.anns.getSize();
			this.newUTF8("RuntimeVisibleAnnotations");
		}
		if (ClassReader.ANNOTATIONS && this.ianns != null)
		{
			++attributeCount;
			size += 8 + this.ianns.getSize();
			this.newUTF8("RuntimeInvisibleAnnotations");
		}
		if (ClassReader.ANNOTATIONS && this.tanns != null)
		{
			++attributeCount;
			size += 8 + this.tanns.getSize();
			this.newUTF8("RuntimeVisibleTypeAnnotations");
		}
		if (ClassReader.ANNOTATIONS && this.itanns != null)
		{
			++attributeCount;
			size += 8 + this.itanns.getSize();
			this.newUTF8("RuntimeInvisibleTypeAnnotations");
		}
		if (this.attrs != null)
		{
			attributeCount += this.attrs.getCount();
			size += this.attrs.getSize(this, null, 0, -1, -1);
		}
		size += this.pool.length;
		// allocates a byte vector of this size, in order to avoid unnecessary
		// arraycopy operations in the ByteVector.enlarge() method
		ByteVector out = new ByteVector(size);
		out.putInt(0xCAFEBABE).putInt(this.version);
		out.putShort(this.index).putByteArray(this.pool.data, 0, this.pool.length);
		int mask = Opcodes.ACC_DEPRECATED | ACC_SYNTHETIC_ATTRIBUTE
				| (this.access & ACC_SYNTHETIC_ATTRIBUTE) / TO_ACC_SYNTHETIC;
		out.putShort(this.access & ~mask).putShort(this.name).putShort(this.superName);
		out.putShort(this.interfaceCount);
		for (int i = 0; i < this.interfaceCount; ++i)
		{
			out.putShort(this.interfaces[i]);
		}
		out.putShort(nbFields);
		fb = this.firstField;
		while (fb != null)
		{
			fb.put(out);
			fb = fb.next;
		}
		out.putShort(nbMethods);
		mb = this.firstMethod;
		while (mb != null)
		{
			mb.put(out);
			mb = mb.next;
		}
		out.putShort(attributeCount);
		if (this.bootstrapMethods != null)
		{
			out.putShort(this.newUTF8("BootstrapMethods"));
			out.putInt(this.bootstrapMethods.length + 2).putShort(this.bootstrapMethodsCount);
			out.putByteArray(this.bootstrapMethods.data, 0, this.bootstrapMethods.length);
		}
		if (ClassReader.SIGNATURES && this.signature != 0)
		{
			out.putShort(this.newUTF8("Signature")).putInt(2).putShort(this.signature);
		}
		if (this.sourceFile != 0)
		{
			out.putShort(this.newUTF8("SourceFile")).putInt(2).putShort(this.sourceFile);
		}
		if (this.sourceDebug != null)
		{
			int len = this.sourceDebug.length;
			out.putShort(this.newUTF8("SourceDebugExtension")).putInt(len);
			out.putByteArray(this.sourceDebug.data, 0, len);
		}
		if (this.enclosingMethodOwner != 0)
		{
			out.putShort(this.newUTF8("EnclosingMethod")).putInt(4);
			out.putShort(this.enclosingMethodOwner).putShort(this.enclosingMethod);
		}
		if ((this.access & Opcodes.ACC_DEPRECATED) != 0)
		{
			out.putShort(this.newUTF8("Deprecated")).putInt(0);
		}
		if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if ((this.version & 0xFFFF) < Opcodes.V1_5 || (this.access & ACC_SYNTHETIC_ATTRIBUTE) != 0)
			{
				out.putShort(this.newUTF8("Synthetic")).putInt(0);
			}
		}
		if (this.innerClasses != null)
		{
			out.putShort(this.newUTF8("InnerClasses"));
			out.putInt(this.innerClasses.length + 2).putShort(this.innerClassesCount);
			out.putByteArray(this.innerClasses.data, 0, this.innerClasses.length);
		}
		if (ClassReader.ANNOTATIONS && this.anns != null)
		{
			out.putShort(this.newUTF8("RuntimeVisibleAnnotations"));
			this.anns.put(out);
		}
		if (ClassReader.ANNOTATIONS && this.ianns != null)
		{
			out.putShort(this.newUTF8("RuntimeInvisibleAnnotations"));
			this.ianns.put(out);
		}
		if (ClassReader.ANNOTATIONS && this.tanns != null)
		{
			out.putShort(this.newUTF8("RuntimeVisibleTypeAnnotations"));
			this.tanns.put(out);
		}
		if (ClassReader.ANNOTATIONS && this.itanns != null)
		{
			out.putShort(this.newUTF8("RuntimeInvisibleTypeAnnotations"));
			this.itanns.put(out);
		}
		if (this.attrs != null)
		{
			this.attrs.put(this, null, 0, -1, -1, out);
		}
		if (this.invalidFrames)
		{
			this.anns = null;
			this.ianns = null;
			this.attrs = null;
			this.innerClassesCount = 0;
			this.innerClasses = null;
			this.bootstrapMethodsCount = 0;
			this.bootstrapMethods = null;
			this.firstField = null;
			this.lastField = null;
			this.firstMethod = null;
			this.lastMethod = null;
			this.computeMaxs = false;
			this.computeFrames = true;
			this.invalidFrames = false;
			new ClassReader(out.data).accept(this, ClassReader.SKIP_FRAMES);
			return this.toByteArray();
		}
		return out.data;
	}
	
	Item newConstItem(final Object cst)
	{
		if (cst instanceof Integer)
		{
			int val = ((Integer) cst).intValue();
			return this.newInteger(val);
		}
		else if (cst instanceof Byte)
		{
			int val = ((Byte) cst).intValue();
			return this.newInteger(val);
		}
		else if (cst instanceof Character)
		{
			int val = ((Character) cst).charValue();
			return this.newInteger(val);
		}
		else if (cst instanceof Short)
		{
			int val = ((Short) cst).intValue();
			return this.newInteger(val);
		}
		else if (cst instanceof Boolean)
		{
			int val = ((Boolean) cst).booleanValue() ? 1 : 0;
			return this.newInteger(val);
		}
		else if (cst instanceof Float)
		{
			float val = ((Float) cst).floatValue();
			return this.newFloat(val);
		}
		else if (cst instanceof Long)
		{
			long val = ((Long) cst).longValue();
			return this.newLong(val);
		}
		else if (cst instanceof Double)
		{
			double val = ((Double) cst).doubleValue();
			return this.newDouble(val);
		}
		else if (cst instanceof String)
		{
			return this.newString((String) cst);
		}
		else if (cst instanceof Type)
		{
			Type t = (Type) cst;
			int s = t.getSort();
			if (s == Type.OBJECT)
			{
				return this.newClassItem(t.getInternalName());
			}
			else if (s == Type.METHOD)
			{
				return this.newMethodTypeItem(t.getDescriptor());
			}
			else
			{ // s == primitive type or array
				return this.newClassItem(t.getDescriptor());
			}
		}
		else if (cst instanceof Handle)
		{
			Handle h = (Handle) cst;
			return this.newHandleItem(h.tag, h.owner, h.name, h.desc);
		}
		else
		{
			throw new IllegalArgumentException("value " + cst);
		}
	}
	
	public int newConst(final Object cst)
	{
		return this.newConstItem(cst).index;
	}
	
	public int newUTF8(final String value)
	{
		this.key.set(UTF8, value, null, null);
		Item result = this.get(this.key);
		if (result == null)
		{
			this.pool.putByte(UTF8).putUTF8(value);
			result = new Item(this.index++, this.key);
			this.put(result);
		}
		return result.index;
	}
	
	Item newClassItem(final String value)
	{
		this.key2.set(CLASS, value, null, null);
		Item result = this.get(this.key2);
		if (result == null)
		{
			this.pool.put12(CLASS, this.newUTF8(value));
			result = new Item(this.index++, this.key2);
			this.put(result);
		}
		return result;
	}
	
	public int newClass(final String value)
	{
		return this.newClassItem(value).index;
	}
	
	Item newMethodTypeItem(final String methodDesc)
	{
		this.key2.set(MTYPE, methodDesc, null, null);
		Item result = this.get(this.key2);
		if (result == null)
		{
			this.pool.put12(MTYPE, this.newUTF8(methodDesc));
			result = new Item(this.index++, this.key2);
			this.put(result);
		}
		return result;
	}
	
	public int newMethodType(final String methodDesc)
	{
		return this.newMethodTypeItem(methodDesc).index;
	}
	
	Item newHandleItem(final int tag, final String owner, final String name, final String desc)
	{
		this.key4.set(HANDLE_BASE + tag, owner, name, desc);
		Item result = this.get(this.key4);
		if (result == null)
		{
			if (tag <= Opcodes.H_PUTSTATIC)
			{
				this.put112(HANDLE, tag, this.newField(owner, name, desc));
			}
			else
			{
				this.put112(HANDLE, tag, this.newMethod(owner, name, desc, tag == Opcodes.H_INVOKEINTERFACE));
			}
			result = new Item(this.index++, this.key4);
			this.put(result);
		}
		return result;
	}
	
	public int newHandle(final int tag, final String owner, final String name, final String desc)
	{
		return this.newHandleItem(tag, owner, name, desc).index;
	}
	
	Item newInvokeDynamicItem(final String name, final String desc, final Handle bsm, final Object... bsmArgs)
	{
		// cache for performance
		ByteVector bootstrapMethods = this.bootstrapMethods;
		if (bootstrapMethods == null)
		{
			bootstrapMethods = this.bootstrapMethods = new ByteVector();
		}
		
		int position = bootstrapMethods.length; // record current position
		
		int hashCode = bsm.hashCode();
		bootstrapMethods.putShort(this.newHandle(bsm.tag, bsm.owner, bsm.name, bsm.desc));
		
		int argsLength = bsmArgs.length;
		bootstrapMethods.putShort(argsLength);
		
		for (int i = 0; i < argsLength; i++)
		{
			Object bsmArg = bsmArgs[i];
			hashCode ^= bsmArg.hashCode();
			bootstrapMethods.putShort(this.newConst(bsmArg));
		}
		
		byte[] data = bootstrapMethods.data;
		int length = 1 + 1 + argsLength << 1; // (bsm + argCount + arguments)
		hashCode &= 0x7FFFFFFF;
		Item result = this.items[hashCode % this.items.length];
		loop:
		while (result != null)
		{
			if (result.type != BSM || result.hashCode != hashCode)
			{
				result = result.next;
				continue;
			}
			
			// because the data encode the size of the argument
			// we don't need to test if these size are equals
			int resultPosition = result.intVal;
			for (int p = 0; p < length; p++)
			{
				if (data[position + p] != data[resultPosition + p])
				{
					result = result.next;
					continue loop;
				}
			}
			break;
		}
		
		int bootstrapMethodIndex;
		if (result != null)
		{
			bootstrapMethodIndex = result.index;
			bootstrapMethods.length = position; // revert to old position
		}
		else
		{
			bootstrapMethodIndex = this.bootstrapMethodsCount++;
			result = new Item(bootstrapMethodIndex);
			result.set(position, hashCode);
			this.put(result);
		}
		
		// now, create the InvokeDynamic constant
		this.key3.set(name, desc, bootstrapMethodIndex);
		result = this.get(this.key3);
		if (result == null)
		{
			this.put122(INDY, bootstrapMethodIndex, this.newNameType(name, desc));
			result = new Item(this.index++, this.key3);
			this.put(result);
		}
		return result;
	}
	
	public int newInvokeDynamic(final String name, final String desc, final Handle bsm, final Object... bsmArgs)
	{
		return this.newInvokeDynamicItem(name, desc, bsm, bsmArgs).index;
	}
	
	Item newFieldItem(final String owner, final String name, final String desc)
	{
		this.key3.set(FIELD, owner, name, desc);
		Item result = this.get(this.key3);
		if (result == null)
		{
			this.put122(FIELD, this.newClass(owner), this.newNameType(name, desc));
			result = new Item(this.index++, this.key3);
			this.put(result);
		}
		return result;
	}
	
	public int newField(final String owner, final String name, final String desc)
	{
		return this.newFieldItem(owner, name, desc).index;
	}
	
	Item newMethodItem(final String owner, final String name, final String desc, final boolean itf)
	{
		int type = itf ? IMETH : METH;
		this.key3.set(type, owner, name, desc);
		Item result = this.get(this.key3);
		if (result == null)
		{
			this.put122(type, this.newClass(owner), this.newNameType(name, desc));
			result = new Item(this.index++, this.key3);
			this.put(result);
		}
		return result;
	}
	
	public int newMethod(final String owner, final String name, final String desc, final boolean itf)
	{
		return this.newMethodItem(owner, name, desc, itf).index;
	}
	
	Item newInteger(final int value)
	{
		this.key.set(value);
		Item result = this.get(this.key);
		if (result == null)
		{
			this.pool.putByte(INT).putInt(value);
			result = new Item(this.index++, this.key);
			this.put(result);
		}
		return result;
	}
	
	Item newFloat(final float value)
	{
		this.key.set(value);
		Item result = this.get(this.key);
		if (result == null)
		{
			this.pool.putByte(FLOAT).putInt(this.key.intVal);
			result = new Item(this.index++, this.key);
			this.put(result);
		}
		return result;
	}
	
	Item newLong(final long value)
	{
		this.key.set(value);
		Item result = this.get(this.key);
		if (result == null)
		{
			this.pool.putByte(LONG).putLong(value);
			result = new Item(this.index, this.key);
			this.index += 2;
			this.put(result);
		}
		return result;
	}
	
	Item newDouble(final double value)
	{
		this.key.set(value);
		Item result = this.get(this.key);
		if (result == null)
		{
			this.pool.putByte(DOUBLE).putLong(this.key.longVal);
			result = new Item(this.index, this.key);
			this.index += 2;
			this.put(result);
		}
		return result;
	}
	
	private Item newString(final String value)
	{
		this.key2.set(STR, value, null, null);
		Item result = this.get(this.key2);
		if (result == null)
		{
			this.pool.put12(STR, this.newUTF8(value));
			result = new Item(this.index++, this.key2);
			this.put(result);
		}
		return result;
	}
	
	public int newNameType(final String name, final String desc)
	{
		return this.newNameTypeItem(name, desc).index;
	}
	
	Item newNameTypeItem(final String name, final String desc)
	{
		this.key2.set(NAME_TYPE, name, desc, null);
		Item result = this.get(this.key2);
		if (result == null)
		{
			this.put122(NAME_TYPE, this.newUTF8(name), this.newUTF8(desc));
			result = new Item(this.index++, this.key2);
			this.put(result);
		}
		return result;
	}
	
	int addType(final String type)
	{
		this.key.set(TYPE_NORMAL, type, null, null);
		Item result = this.get(this.key);
		if (result == null)
		{
			result = this.addType(this.key);
		}
		return result.index;
	}
	
	int addUninitializedType(final String type, final int offset)
	{
		this.key.type = TYPE_UNINIT;
		this.key.intVal = offset;
		this.key.strVal1 = type;
		this.key.hashCode = 0x7FFFFFFF & TYPE_UNINIT + type.hashCode() + offset;
		Item result = this.get(this.key);
		if (result == null)
		{
			result = this.addType(this.key);
		}
		return result.index;
	}
	
	private Item addType(final Item item)
	{
		++this.typeCount;
		Item result = new Item(this.typeCount, this.key);
		this.put(result);
		if (this.typeTable == null)
		{
			this.typeTable = new Item[16];
		}
		if (this.typeCount == this.typeTable.length)
		{
			Item[] newTable = new Item[2 * this.typeTable.length];
			System.arraycopy(this.typeTable, 0, newTable, 0, this.typeTable.length);
			this.typeTable = newTable;
		}
		this.typeTable[this.typeCount] = result;
		return result;
	}
	
	int getMergedType(final int type1, final int type2)
	{
		this.key2.type = TYPE_MERGED;
		this.key2.longVal = type1 | (long) type2 << 32;
		this.key2.hashCode = 0x7FFFFFFF & TYPE_MERGED + type1 + type2;
		Item result = this.get(this.key2);
		if (result == null)
		{
			String t = this.typeTable[type1].strVal1;
			String u = this.typeTable[type2].strVal1;
			this.key2.intVal = this.addType(this.getCommonSuperClass(t, u));
			result = new Item((short) 0, this.key2);
			this.put(result);
		}
		return result.intVal;
	}
	
	protected String getCommonSuperClass(final String type1, final String type2)
	{
		Class<?> c, d;
		ClassLoader classLoader = this.getClass().getClassLoader();
		try
		{
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d))
		{
			return type1;
		}
		if (d.isAssignableFrom(c))
		{
			return type2;
		}
		if (c.isInterface() || d.isInterface())
		{
			return "java/lang/Object";
		}
		do
		{
			c = c.getSuperclass();
		}
		while (!c.isAssignableFrom(d));
		return c.getName().replace('.', '/');
	}
	
	private Item get(final Item key)
	{
		Item i = this.items[key.hashCode % this.items.length];
		while (i != null && (i.type != key.type || !key.isEqualTo(i)))
		{
			i = i.next;
		}
		return i;
	}
	
	private void put(final Item i)
	{
		if (this.index + this.typeCount > this.threshold)
		{
			int ll = this.items.length;
			int nl = ll * 2 + 1;
			Item[] newItems = new Item[nl];
			for (int l = ll - 1; l >= 0; --l)
			{
				Item j = this.items[l];
				while (j != null)
				{
					int index = j.hashCode % newItems.length;
					Item k = j.next;
					j.next = newItems[index];
					newItems[index] = j;
					j = k;
				}
			}
			this.items = newItems;
			this.threshold = (int) (nl * 0.75);
		}
		int index = i.hashCode % this.items.length;
		i.next = this.items[index];
		this.items[index] = i;
	}
	
	private void put122(final int b, final int s1, final int s2)
	{
		this.pool.put12(b, s1).putShort(s2);
	}
	
	private void put112(final int b1, final int b2, final int s)
	{
		this.pool.put11(b1, b2).putShort(s);
	}
}
