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
package dyvilx.tools.asm;

final class Item
{
	int    index;
	int    type;
	int    intVal;
	long   longVal;
	String strVal1;
	String strVal2;
	String strVal3;
	int    hashCode;
	Item   next;
	
	Item()
	{
	}
	
	Item(final int index)
	{
		this.index = index;
	}
	
	Item(final int index, final Item i)
	{
		this.index = index;
		this.type = i.type;
		this.intVal = i.intVal;
		this.longVal = i.longVal;
		this.strVal1 = i.strVal1;
		this.strVal2 = i.strVal2;
		this.strVal3 = i.strVal3;
		this.hashCode = i.hashCode;
	}
	
	void set(final int intVal)
	{
		this.type = ClassWriter.INT;
		this.intVal = intVal;
		this.hashCode = 0x7FFFFFFF & this.type + intVal;
	}
	
	void set(final long longVal)
	{
		this.type = ClassWriter.LONG;
		this.longVal = longVal;
		this.hashCode = 0x7FFFFFFF & this.type + (int) longVal;
	}
	
	void set(final float floatVal)
	{
		this.type = ClassWriter.FLOAT;
		this.intVal = Float.floatToRawIntBits(floatVal);
		this.hashCode = 0x7FFFFFFF & this.type + (int) floatVal;
	}
	
	void set(final double doubleVal)
	{
		this.type = ClassWriter.DOUBLE;
		this.longVal = Double.doubleToRawLongBits(doubleVal);
		this.hashCode = 0x7FFFFFFF & this.type + (int) doubleVal;
	}
	
	@SuppressWarnings("fallthrough")
	void set(final int type, final String strVal1, final String strVal2, final String strVal3)
	{
		this.type = type;
		this.strVal1 = strVal1;
		this.strVal2 = strVal2;
		this.strVal3 = strVal3;
		switch (type)
		{
		case ClassWriter.CLASS:
			this.intVal = 0; // intVal of a class must be zero, see
			// visitInnerClass
		case ClassWriter.UTF8:
		case ClassWriter.STR:
		case ClassWriter.MTYPE:
		case ClassWriter.TYPE_NORMAL:
			this.hashCode = 0x7FFFFFFF & type + strVal1.hashCode();
			return;
		case ClassWriter.NAME_TYPE:
		{
			this.hashCode = 0x7FFFFFFF & type + strVal1.hashCode() * strVal2.hashCode();
			return;
		}
		// ClassWriter.FIELD:
		// ClassWriter.METH:
		// ClassWriter.IMETH:
		// ClassWriter.HANDLE_BASE + 1..9
		default:
			this.hashCode = 0x7FFFFFFF & type + strVal1.hashCode() * strVal2.hashCode() * strVal3.hashCode();
		}
	}
	
	void set(String name, String desc, int bsmIndex)
	{
		this.type = ClassWriter.INDY;
		this.longVal = bsmIndex;
		this.strVal1 = name;
		this.strVal2 = desc;
		this.hashCode = 0x7FFFFFFF & ClassWriter.INDY + bsmIndex * this.strVal1.hashCode() * this.strVal2.hashCode();
	}
	
	void set(int position, int hashCode)
	{
		this.type = ClassWriter.BSM;
		this.intVal = position;
		this.hashCode = hashCode;
	}
	
	boolean isEqualTo(final Item i)
	{
		switch (this.type)
		{
		case ClassWriter.UTF8:
		case ClassWriter.STR:
		case ClassWriter.CLASS:
		case ClassWriter.MTYPE:
		case ClassWriter.TYPE_NORMAL:
			return i.strVal1.equals(this.strVal1);
		case ClassWriter.TYPE_MERGED:
		case ClassWriter.LONG:
		case ClassWriter.DOUBLE:
			return i.longVal == this.longVal;
		case ClassWriter.INT:
		case ClassWriter.FLOAT:
			return i.intVal == this.intVal;
		case ClassWriter.TYPE_UNINIT:
			return i.intVal == this.intVal && i.strVal1.equals(this.strVal1);
		case ClassWriter.NAME_TYPE:
			return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2);
		case ClassWriter.INDY:
		{
			return i.longVal == this.longVal && i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2);
		}
		// case ClassWriter.FIELD:
		// case ClassWriter.METH:
		// case ClassWriter.IMETH:
		// case ClassWriter.HANDLE_BASE + 1..9
		default:
			return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2) && i.strVal3.equals(this.strVal3);
		}
	}
}
