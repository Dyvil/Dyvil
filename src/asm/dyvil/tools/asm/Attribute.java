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

public class Attribute
{
	public final String type;
	byte[]    value;
	Attribute next;
	
	protected Attribute(final String type)
	{
		this.type = type;
	}
	
	public boolean isUnknown()
	{
		return true;
	}
	
	public boolean isCodeAttribute()
	{
		return false;
	}
	
	protected Label[] getLabels()
	{
		return null;
	}
	
	protected Attribute read(final ClassReader cr, final int off, final int len, final char[] buf, final int codeOff, final Label[] labels)
	{
		Attribute attr = new Attribute(this.type);
		attr.value = new byte[len];
		System.arraycopy(cr.b, off, attr.value, 0, len);
		return attr;
	}
	
	protected ByteVector write(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals)
	{
		ByteVector v = new ByteVector();
		v.data = this.value;
		v.length = this.value.length;
		return v;
	}
	
	final int getCount()
	{
		int count = 0;
		Attribute attr = this;
		while (attr != null)
		{
			count += 1;
			attr = attr.next;
		}
		return count;
	}
	
	final int getSize(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals)
	{
		Attribute attr = this;
		int size = 0;
		while (attr != null)
		{
			cw.newUTF8(attr.type);
			size += attr.write(cw, code, len, maxStack, maxLocals).length + 6;
			attr = attr.next;
		}
		return size;
	}
	
	final void put(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals, final ByteVector out)
	{
		Attribute attr = this;
		while (attr != null)
		{
			ByteVector b = attr.write(cw, code, len, maxStack, maxLocals);
			out.putShort(cw.newUTF8(attr.type)).putInt(b.length);
			out.putByteArray(b.data, 0, b.length);
			attr = attr.next;
		}
	}
}
