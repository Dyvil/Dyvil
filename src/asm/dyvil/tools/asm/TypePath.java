/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2013 INRIA, France Telecom All
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

public class TypePath
{
	public final static int ARRAY_ELEMENT  = 0;
	public final static int INNER_TYPE     = 1;
	public final static int WILDCARD_BOUND = 2;
	public final static int TYPE_ARGUMENT  = 3;

	public static final TypePath EMPTY = new TypePath(new byte[] { 0 }, 0);
	
	byte[] b;
	int    offset;
	
	TypePath(byte[] b, int offset)
	{
		this.b = b;
		this.offset = offset;
	}
	
	public int getLength()
	{
		return this.b[this.offset];
	}
	
	public int getStep(int index)
	{
		return this.b[this.offset + 2 * index + 1];
	}
	
	public int getStepArgument(int index)
	{
		return this.b[this.offset + 2 * index + 2];
	}
	
	public static TypePath fromString(final String typePath)
	{
		if (typePath == null || typePath.isEmpty())
		{
			return null;
		}
		
		int n = typePath.length();
		ByteVector out = new ByteVector(n);
		out.putByte(0);
		for (int i = 0; i < n; )
		{
			char c = typePath.charAt(i++);
			if (c == '[')
			{
				out.put11(ARRAY_ELEMENT, 0);
			}
			else if (c == '.')
			{
				out.put11(INNER_TYPE, 0);
			}
			else if (c == '*')
			{
				out.put11(WILDCARD_BOUND, 0);
			}
			else if (c >= '0' && c <= '9')
			{
				int typeArg = c - '0';
				while (i < n && (c = typePath.charAt(i)) >= '0' && c <= '9')
				{
					typeArg = typeArg * 10 + c - '0';
					i += 1;
				}
				if (i < n && typePath.charAt(i) == ';')
				{
					i += 1;
				}
				out.put11(TYPE_ARGUMENT, typeArg);
			}
		}
		out.data[0] = (byte) (out.length / 2);
		return new TypePath(out.data, 0);
	}
	
	@Override
	public String toString()
	{
		int length = this.getLength();
		StringBuilder result = new StringBuilder(length * 2);
		for (int i = 0; i < length; ++i)
		{
			switch (this.getStep(i))
			{
			case ARRAY_ELEMENT:
				result.append('[');
				break;
			case INNER_TYPE:
				result.append('.');
				break;
			case WILDCARD_BOUND:
				result.append('*');
				break;
			case TYPE_ARGUMENT:
				result.append(this.getStepArgument(i)).append(';');
				break;
			default:
				result.append('_');
			}
		}
		return result.toString();
	}
}
