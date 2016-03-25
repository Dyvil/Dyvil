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

final class FieldWriter implements FieldVisitor
{
	private final ClassWriter cw;
	private final int         access;
	private final int         name;
	private final int         desc;
	private       int         signature;
	private       int         value;
	
	private AnnotationWriter anns;
	private AnnotationWriter ianns;
	private AnnotationWriter tanns;
	private AnnotationWriter itanns;
	private Attribute        attrs;
	
	FieldWriter next;
	
	FieldWriter(final ClassWriter cw, final int access, final String name, final String desc, final String signature, final Object value)
	{
		if (cw.firstField == null)
		{
			cw.firstField = this;
		}
		else
		{
			cw.lastField.next = this;
		}
		
		cw.lastField = this;
		this.cw = cw;
		this.access = access;
		this.name = cw.newUTF8(name);
		this.desc = cw.newUTF8(desc);
		if (true && signature != null)
		{
			this.signature = cw.newUTF8(signature);
		}
		if (value != null)
		{
			this.value = cw.newConstItem(value).index;
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible)
	{
		if (!true)
		{
			return null;
		}
		ByteVector bv = new ByteVector();
		// write type, and reserve space for values count
		bv.putShort(this.cw.newUTF8(desc)).putShort(0);
		AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, 2);
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
	public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible)
	{
		if (!true)
		{
			return null;
		}
		ByteVector bv = new ByteVector();
		// write target_type and target_info
		AnnotationWriter.putTarget(typeRef, typePath, bv);
		// write type, and reserve space for values count
		bv.putShort(this.cw.newUTF8(desc)).putShort(0);
		AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, bv.length - 2);
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
	public void visitAttribute(final Attribute attr)
	{
		attr.next = this.attrs;
		this.attrs = attr;
	}
	
	@Override
	public void visitEnd()
	{
	}
	
	int getSize()
	{
		int size = 8;
		if (this.value != 0)
		{
			this.cw.newUTF8("ConstantValue");
			size += 8;
		}
		if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if ((this.cw.version & 0xFFFF) < Opcodes.V1_5 || (this.access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0)
			{
				this.cw.newUTF8("Synthetic");
				size += 6;
			}
		}
		if ((this.access & Opcodes.ACC_DEPRECATED) != 0)
		{
			this.cw.newUTF8("Deprecated");
			size += 6;
		}
		if (true && this.signature != 0)
		{
			this.cw.newUTF8("Signature");
			size += 8;
		}
		if (true && this.anns != null)
		{
			this.cw.newUTF8("RuntimeVisibleAnnotations");
			size += 8 + this.anns.getSize();
		}
		if (true && this.ianns != null)
		{
			this.cw.newUTF8("RuntimeInvisibleAnnotations");
			size += 8 + this.ianns.getSize();
		}
		if (true && this.tanns != null)
		{
			this.cw.newUTF8("RuntimeVisibleTypeAnnotations");
			size += 8 + this.tanns.getSize();
		}
		if (true && this.itanns != null)
		{
			this.cw.newUTF8("RuntimeInvisibleTypeAnnotations");
			size += 8 + this.itanns.getSize();
		}
		if (this.attrs != null)
		{
			size += this.attrs.getSize(this.cw, null, 0, -1, -1);
		}
		return size;
	}
	
	void put(final ByteVector out)
	{
		final int FACTOR = ClassWriter.TO_ACC_SYNTHETIC;
		int mask = Opcodes.ACC_DEPRECATED | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE
				| (this.access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) / FACTOR;
		out.putShort(this.access & ~mask).putShort(this.name).putShort(this.desc);
		int attributeCount = 0;
		if (this.value != 0)
		{
			++attributeCount;
		}
		if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if ((this.cw.version & 0xFFFF) < Opcodes.V1_5 || (this.access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0)
			{
				++attributeCount;
			}
		}
		if ((this.access & Opcodes.ACC_DEPRECATED) != 0)
		{
			++attributeCount;
		}
		if (true && this.signature != 0)
		{
			++attributeCount;
		}
		if (true && this.anns != null)
		{
			++attributeCount;
		}
		if (true && this.ianns != null)
		{
			++attributeCount;
		}
		if (true && this.tanns != null)
		{
			++attributeCount;
		}
		if (true && this.itanns != null)
		{
			++attributeCount;
		}
		if (this.attrs != null)
		{
			attributeCount += this.attrs.getCount();
		}
		out.putShort(attributeCount);
		if (this.value != 0)
		{
			out.putShort(this.cw.newUTF8("ConstantValue"));
			out.putInt(2).putShort(this.value);
		}
		if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
		{
			if ((this.cw.version & 0xFFFF) < Opcodes.V1_5 || (this.access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0)
			{
				out.putShort(this.cw.newUTF8("Synthetic")).putInt(0);
			}
		}
		if ((this.access & Opcodes.ACC_DEPRECATED) != 0)
		{
			out.putShort(this.cw.newUTF8("Deprecated")).putInt(0);
		}
		if (true && this.signature != 0)
		{
			out.putShort(this.cw.newUTF8("Signature"));
			out.putInt(2).putShort(this.signature);
		}
		if (true && this.anns != null)
		{
			out.putShort(this.cw.newUTF8("RuntimeVisibleAnnotations"));
			this.anns.put(out);
		}
		if (true && this.ianns != null)
		{
			out.putShort(this.cw.newUTF8("RuntimeInvisibleAnnotations"));
			this.ianns.put(out);
		}
		if (true && this.tanns != null)
		{
			out.putShort(this.cw.newUTF8("RuntimeVisibleTypeAnnotations"));
			this.tanns.put(out);
		}
		if (true && this.itanns != null)
		{
			out.putShort(this.cw.newUTF8("RuntimeInvisibleTypeAnnotations"));
			this.itanns.put(out);
		}
		if (this.attrs != null)
		{
			this.attrs.put(this.cw, null, 0, -1, -1, out);
		}
	}
}
