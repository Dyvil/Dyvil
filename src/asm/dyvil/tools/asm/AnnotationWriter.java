/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
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
package dyvil.tools.asm;

final class AnnotationWriter implements AnnotationVisitor
{
	private final ClassWriter cw;
	private       int         size;
	private final boolean     named;
	private final ByteVector  bv;
	private final ByteVector  parent;
	private final int         offset;
	AnnotationWriter next;
	AnnotationWriter prev;
	
	AnnotationWriter(final ClassWriter cw, final boolean named, final ByteVector bv, final ByteVector parent, final int offset)
	{
		this.cw = cw;
		this.named = named;
		this.bv = bv;
		this.parent = parent;
		this.offset = offset;
	}
	
	@Override
	public void visit(final String name, final Object value)
	{
		++this.size;
		if (this.named)
		{
			this.bv.putShort(this.cw.newUTF8(name));
		}
		if (value instanceof String)
		{
			this.bv.put12('s', this.cw.newUTF8((String) value));
		}
		else if (value instanceof Byte)
		{
			this.bv.put12('B', this.cw.newInteger(((Byte) value).byteValue()).index);
		}
		else if (value instanceof Boolean)
		{
			int v = ((Boolean) value).booleanValue() ? 1 : 0;
			this.bv.put12('Z', this.cw.newInteger(v).index);
		}
		else if (value instanceof Character)
		{
			this.bv.put12('C', this.cw.newInteger(((Character) value).charValue()).index);
		}
		else if (value instanceof Short)
		{
			this.bv.put12('S', this.cw.newInteger(((Short) value).shortValue()).index);
		}
		else if (value instanceof Type)
		{
			this.bv.put12('c', this.cw.newUTF8(((Type) value).getDescriptor()));
		}
		else if (value instanceof byte[])
		{
			byte[] v = (byte[]) value;
			this.bv.put12('[', v.length);
			for (byte element : v)
			{
				this.bv.put12('B', this.cw.newInteger(element).index);
			}
		}
		else if (value instanceof boolean[])
		{
			boolean[] v = (boolean[]) value;
			this.bv.put12('[', v.length);
			for (boolean element : v)
			{
				this.bv.put12('Z', this.cw.newInteger(element ? 1 : 0).index);
			}
		}
		else if (value instanceof short[])
		{
			short[] v = (short[]) value;
			this.bv.put12('[', v.length);
			for (short element : v)
			{
				this.bv.put12('S', this.cw.newInteger(element).index);
			}
		}
		else if (value instanceof char[])
		{
			char[] v = (char[]) value;
			this.bv.put12('[', v.length);
			for (char element : v)
			{
				this.bv.put12('C', this.cw.newInteger(element).index);
			}
		}
		else if (value instanceof int[])
		{
			int[] v = (int[]) value;
			this.bv.put12('[', v.length);
			for (int element : v)
			{
				this.bv.put12('I', this.cw.newInteger(element).index);
			}
		}
		else if (value instanceof long[])
		{
			long[] v = (long[]) value;
			this.bv.put12('[', v.length);
			for (long element : v)
			{
				this.bv.put12('J', this.cw.newLong(element).index);
			}
		}
		else if (value instanceof float[])
		{
			float[] v = (float[]) value;
			this.bv.put12('[', v.length);
			for (float element : v)
			{
				this.bv.put12('F', this.cw.newFloat(element).index);
			}
		}
		else if (value instanceof double[])
		{
			double[] v = (double[]) value;
			this.bv.put12('[', v.length);
			for (double element : v)
			{
				this.bv.put12('D', this.cw.newDouble(element).index);
			}
		}
		else
		{
			Item i = this.cw.newConstItem(value);
			this.bv.put12(".s.IFJDCS".charAt(i.type), i.index);
		}
	}
	
	@Override
	public void visitEnum(final String name, final String desc, final String value)
	{
		++this.size;
		if (this.named)
		{
			this.bv.putShort(this.cw.newUTF8(name));
		}
		this.bv.put12('e', this.cw.newUTF8(desc)).putShort(this.cw.newUTF8(value));
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(final String name, final String desc)
	{
		++this.size;
		if (this.named)
		{
			this.bv.putShort(this.cw.newUTF8(name));
		}
		// write tag and type, and reserve space for values count
		this.bv.put12('@', this.cw.newUTF8(desc)).putShort(0);
		return new AnnotationWriter(this.cw, true, this.bv, this.bv, this.bv.length - 2);
	}
	
	@Override
	public AnnotationVisitor visitArray(final String name)
	{
		++this.size;
		if (this.named)
		{
			this.bv.putShort(this.cw.newUTF8(name));
		}
		// write tag, and reserve space for array size
		this.bv.put12('[', 0);
		return new AnnotationWriter(this.cw, false, this.bv, this.bv, this.bv.length - 2);
	}
	
	@Override
	public void visitEnd()
	{
		if (this.parent != null)
		{
			byte[] data = this.parent.data;
			data[this.offset] = (byte) (this.size >>> 8);
			data[this.offset + 1] = (byte) this.size;
		}
	}
	
	int getSize()
	{
		int size = 0;
		AnnotationWriter aw = this;
		while (aw != null)
		{
			size += aw.bv.length;
			aw = aw.next;
		}
		return size;
	}
	
	void put(final ByteVector out)
	{
		int n = 0;
		int size = 2;
		AnnotationWriter aw = this;
		AnnotationWriter last = null;
		while (aw != null)
		{
			++n;
			size += aw.bv.length;
			aw.visitEnd(); // in case user forgot to call visitEnd
			aw.prev = last;
			last = aw;
			aw = aw.next;
		}
		out.putInt(size);
		out.putShort(n);
		aw = last;
		while (aw != null)
		{
			out.putByteArray(aw.bv.data, 0, aw.bv.length);
			aw = aw.prev;
		}
	}
	
	static void put(final AnnotationWriter[] panns, final int off, final ByteVector out)
	{
		int size = 1 + 2 * (panns.length - off);
		for (int i = off; i < panns.length; ++i)
		{
			size += panns[i] == null ? 0 : panns[i].getSize();
		}
		out.putInt(size).putByte(panns.length - off);
		for (int i = off; i < panns.length; ++i)
		{
			AnnotationWriter aw = panns[i];
			AnnotationWriter last = null;
			int n = 0;
			while (aw != null)
			{
				++n;
				aw.visitEnd(); // in case user forgot to call visitEnd
				aw.prev = last;
				last = aw;
				aw = aw.next;
			}
			out.putShort(n);
			aw = last;
			while (aw != null)
			{
				out.putByteArray(aw.bv.data, 0, aw.bv.length);
				aw = aw.prev;
			}
		}
	}
	
	static void putTarget(int typeRef, TypePath typePath, ByteVector out)
	{
		switch (typeRef >>> 24)
		{
		case 0x00: // CLASS_TYPE_PARAMETER
		case 0x01: // METHOD_TYPE_PARAMETER
		case 0x16: // METHOD_FORMAL_PARAMETER
			out.putShort(typeRef >>> 16);
			break;
		case 0x13: // FIELD
		case 0x14: // METHOD_RETURN
		case 0x15: // METHOD_RECEIVER
			out.putByte(typeRef >>> 24);
			break;
		case 0x47: // CAST
		case 0x48: // CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
		case 0x49: // METHOD_INVOCATION_TYPE_ARGUMENT
		case 0x4A: // CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
		case 0x4B: // METHOD_REFERENCE_TYPE_ARGUMENT
			out.putInt(typeRef);
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
			out.put12(typeRef >>> 24, (typeRef & 0xFFFF00) >> 8);
			break;
		}
		if (typePath == null)
		{
			out.putByte(0);
		}
		else
		{
			int length = typePath.b[typePath.offset] * 2 + 1;
			out.putByteArray(typePath.b, typePath.offset, length);
		}
	}
}
