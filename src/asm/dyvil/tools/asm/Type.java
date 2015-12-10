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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Type
{
	public static final int  VOID         = 0;
	public static final int  BOOLEAN      = 1;
	public static final int  CHAR         = 2;
	public static final int  BYTE         = 3;
	public static final int  SHORT        = 4;
	public static final int  INT          = 5;
	public static final int  FLOAT        = 6;
	public static final int  LONG         = 7;
	public static final int  DOUBLE       = 8;
	public static final int  ARRAY        = 9;
	public static final int  OBJECT       = 10;
	public static final int  METHOD       = 11;
	public static final Type VOID_TYPE    = new Type(VOID, null, 'V' << 24 | 5 << 16 | 0 << 8 | 0, 1);
	public static final Type BOOLEAN_TYPE = new Type(BOOLEAN, null, 'Z' << 24 | 0 << 16 | 5 << 8 | 1, 1);
	public static final Type CHAR_TYPE    = new Type(CHAR, null, 'C' << 24 | 0 << 16 | 6 << 8 | 1, 1);
	public static final Type BYTE_TYPE    = new Type(BYTE, null, 'B' << 24 | 0 << 16 | 5 << 8 | 1, 1);
	public static final Type SHORT_TYPE   = new Type(SHORT, null, 'S' << 24 | 0 << 16 | 7 << 8 | 1, 1);
	public static final Type INT_TYPE     = new Type(INT, null, 'I' << 24 | 0 << 16 | 0 << 8 | 1, 1);
	public static final Type FLOAT_TYPE   = new Type(FLOAT, null, 'F' << 24 | 2 << 16 | 2 << 8 | 1, 1);
	public static final Type LONG_TYPE    = new Type(LONG, null, 'J' << 24 | 1 << 16 | 1 << 8 | 2, 1);
	public static final Type DOUBLE_TYPE  = new Type(DOUBLE, null, 'D' << 24 | 3 << 16 | 3 << 8 | 2, 1);
	
	private final int    sort;
	private final char[] buf;
	private final int    off;
	private final int    len;
	
	private Type(final int sort, final char[] buf, final int off, final int len)
	{
		this.sort = sort;
		this.buf = buf;
		this.off = off;
		this.len = len;
	}
	
	public static Type getType(final String typeDescriptor)
	{
		return getType(typeDescriptor.toCharArray(), 0);
	}
	
	public static Type getObjectType(final String internalName)
	{
		char[] buf = internalName.toCharArray();
		return new Type(buf[0] == '[' ? ARRAY : OBJECT, buf, 0, buf.length);
	}
	
	public static Type getMethodType(final String methodDescriptor)
	{
		return getType(methodDescriptor.toCharArray(), 0);
	}
	
	public static Type getMethodType(final Type returnType, final Type... argumentTypes)
	{
		return getType(getMethodDescriptor(returnType, argumentTypes));
	}
	
	public static Type getType(final Class<?> c)
	{
		if (c.isPrimitive())
		{
			if (c == Integer.TYPE)
			{
				return INT_TYPE;
			}
			else if (c == Void.TYPE)
			{
				return VOID_TYPE;
			}
			else if (c == Boolean.TYPE)
			{
				return BOOLEAN_TYPE;
			}
			else if (c == Byte.TYPE)
			{
				return BYTE_TYPE;
			}
			else if (c == Character.TYPE)
			{
				return CHAR_TYPE;
			}
			else if (c == Short.TYPE)
			{
				return SHORT_TYPE;
			}
			else if (c == Double.TYPE)
			{
				return DOUBLE_TYPE;
			}
			else if (c == Float.TYPE)
			{
				return FLOAT_TYPE;
			}
			else
			/* if (c == Long.TYPE) */
			{
				return LONG_TYPE;
			}
		}
		return getType(getDescriptor(c));
	}
	
	public static Type getType(final Constructor<?> c)
	{
		return getType(getConstructorDescriptor(c));
	}
	
	public static Type getType(final Method m)
	{
		return getType(getMethodDescriptor(m));
	}
	
	public static Type[] getArgumentTypes(final String methodDescriptor)
	{
		char[] buf = methodDescriptor.toCharArray();
		int off = 1;
		int size = 0;
		while (true)
		{
			char car = buf[off++];
			if (car == ')')
			{
				break;
			}
			else if (car == 'L')
			{
				while (buf[off++] != ';')
				{
				}
				++size;
			}
			else if (car != '[')
			{
				++size;
			}
		}
		Type[] args = new Type[size];
		off = 1;
		size = 0;
		while (buf[off] != ')')
		{
			args[size] = getType(buf, off);
			off += args[size].len + (args[size].sort == OBJECT ? 2 : 0);
			size += 1;
		}
		return args;
	}
	
	public static Type[] getArgumentTypes(final Method method)
	{
		Class<?>[] classes = method.getParameterTypes();
		Type[] types = new Type[classes.length];
		for (int i = classes.length - 1; i >= 0; --i)
		{
			types[i] = getType(classes[i]);
		}
		return types;
	}
	
	public static Type getReturnType(final String methodDescriptor)
	{
		char[] buf = methodDescriptor.toCharArray();
		return getType(buf, methodDescriptor.indexOf(')') + 1);
	}
	
	public static Type getReturnType(final Method method)
	{
		return getType(method.getReturnType());
	}
	
	public static int getArgumentsAndReturnSizes(final String desc)
	{
		int n = 1;
		int c = 1;
		while (true)
		{
			char car = desc.charAt(c++);
			if (car == ')')
			{
				car = desc.charAt(c);
				return n << 2 | (car == 'V' ? 0 : car == 'D' || car == 'J' ? 2 : 1);
			}
			else if (car == 'L')
			{
				while (desc.charAt(c++) != ';')
				{
				}
				n += 1;
			}
			else if (car == '[')
			{
				while ((car = desc.charAt(c)) == '[')
				{
					++c;
				}
				if (car == 'D' || car == 'J')
				{
					n -= 1;
				}
			}
			else if (car == 'D' || car == 'J')
			{
				n += 2;
			}
			else
			{
				n += 1;
			}
		}
	}
	
	private static Type getType(final char[] buf, final int off)
	{
		int len;
		switch (buf[off])
		{
		case 'V':
			return VOID_TYPE;
		case 'Z':
			return BOOLEAN_TYPE;
		case 'C':
			return CHAR_TYPE;
		case 'B':
			return BYTE_TYPE;
		case 'S':
			return SHORT_TYPE;
		case 'I':
			return INT_TYPE;
		case 'F':
			return FLOAT_TYPE;
		case 'J':
			return LONG_TYPE;
		case 'D':
			return DOUBLE_TYPE;
		case '[':
			len = 1;
			while (buf[off + len] == '[')
			{
				++len;
			}
			if (buf[off + len] == 'L')
			{
				++len;
				while (buf[off + len] != ';')
				{
					++len;
				}
			}
			return new Type(ARRAY, buf, off, len + 1);
		case 'L':
			len = 1;
			while (buf[off + len] != ';')
			{
				++len;
			}
			return new Type(OBJECT, buf, off + 1, len - 1);
		// case '(':
		default:
			return new Type(METHOD, buf, off, buf.length - off);
		}
	}
	
	public int getSort()
	{
		return this.sort;
	}
	
	public int getDimensions()
	{
		int i = 1;
		while (this.buf[this.off + i] == '[')
		{
			++i;
		}
		return i;
	}
	
	public Type getElementType()
	{
		return getType(this.buf, this.off + this.getDimensions());
	}
	
	public String getClassName()
	{
		switch (this.sort)
		{
		case VOID:
			return "void";
		case BOOLEAN:
			return "boolean";
		case CHAR:
			return "char";
		case BYTE:
			return "byte";
		case SHORT:
			return "short";
		case INT:
			return "int";
		case FLOAT:
			return "float";
		case LONG:
			return "long";
		case DOUBLE:
			return "double";
		case ARRAY:
			StringBuilder sb = new StringBuilder(this.getElementType().getClassName());
			for (int i = this.getDimensions(); i > 0; --i)
			{
				sb.append("[]");
			}
			return sb.toString();
		case OBJECT:
			return new String(this.buf, this.off, this.len).replace('/', '.');
		default:
			return null;
		}
	}
	
	public String getInternalName()
	{
		return new String(this.buf, this.off, this.len);
	}
	
	public Type[] getArgumentTypes()
	{
		return getArgumentTypes(this.getDescriptor());
	}
	
	public Type getReturnType()
	{
		return getReturnType(this.getDescriptor());
	}
	
	public int getArgumentsAndReturnSizes()
	{
		return getArgumentsAndReturnSizes(this.getDescriptor());
	}
	
	public String getDescriptor()
	{
		StringBuffer buf = new StringBuffer();
		this.getDescriptor(buf);
		return buf.toString();
	}
	
	public static String getMethodDescriptor(final Type returnType, final Type... argumentTypes)
	{
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		for (Type argumentType : argumentTypes)
		{
			argumentType.getDescriptor(buf);
		}
		buf.append(')');
		returnType.getDescriptor(buf);
		return buf.toString();
	}
	
	private void getDescriptor(final StringBuffer buf)
	{
		if (this.buf == null)
		{
			// descriptor is in byte 3 of 'off' for primitive types (buf ==
			// null)
			buf.append((char) ((this.off & 0xFF000000) >>> 24));
		}
		else if (this.sort == OBJECT)
		{
			buf.append('L');
			buf.append(this.buf, this.off, this.len);
			buf.append(';');
		}
		else
		{ // sort == ARRAY || sort == METHOD
			buf.append(this.buf, this.off, this.len);
		}
	}
	
	public static String getInternalName(final Class<?> c)
	{
		return c.getName().replace('.', '/');
	}
	
	public static String getDescriptor(final Class<?> c)
	{
		StringBuffer buf = new StringBuffer();
		getDescriptor(buf, c);
		return buf.toString();
	}
	
	public static String getConstructorDescriptor(final Constructor<?> c)
	{
		Class<?>[] parameters = c.getParameterTypes();
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		for (Class<?> parameter : parameters)
		{
			getDescriptor(buf, parameter);
		}
		return buf.append(")V").toString();
	}
	
	public static String getMethodDescriptor(final Method m)
	{
		Class<?>[] parameters = m.getParameterTypes();
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		for (Class<?> parameter : parameters)
		{
			getDescriptor(buf, parameter);
		}
		buf.append(')');
		getDescriptor(buf, m.getReturnType());
		return buf.toString();
	}
	
	private static void getDescriptor(final StringBuffer buf, final Class<?> c)
	{
		Class<?> d = c;
		while (true)
		{
			if (d.isPrimitive())
			{
				char car;
				if (d == Integer.TYPE)
				{
					car = 'I';
				}
				else if (d == Void.TYPE)
				{
					car = 'V';
				}
				else if (d == Boolean.TYPE)
				{
					car = 'Z';
				}
				else if (d == Byte.TYPE)
				{
					car = 'B';
				}
				else if (d == Character.TYPE)
				{
					car = 'C';
				}
				else if (d == Short.TYPE)
				{
					car = 'S';
				}
				else if (d == Double.TYPE)
				{
					car = 'D';
				}
				else if (d == Float.TYPE)
				{
					car = 'F';
				}
				else
				/* if (d == Long.TYPE) */
				{
					car = 'J';
				}
				buf.append(car);
				return;
			}
			else if (d.isArray())
			{
				buf.append('[');
				d = d.getComponentType();
			}
			else
			{
				buf.append('L');
				String name = d.getName();
				int len = name.length();
				for (int i = 0; i < len; ++i)
				{
					char car = name.charAt(i);
					buf.append(car == '.' ? '/' : car);
				}
				buf.append(';');
				return;
			}
		}
	}
	
	public int getSize()
	{
		// the size is in byte 0 of 'off' for primitive types (buf == null)
		return this.buf == null ? this.off & 0xFF : 1;
	}
	
	public int getOpcode(final int opcode)
	{
		if (opcode == Opcodes.IALOAD || opcode == Opcodes.IASTORE)
		{
			// the offset for IALOAD or IASTORE is in byte 1 of 'off' for
			// primitive types (buf == null)
			return opcode + (this.buf == null ? (this.off & 0xFF00) >> 8 : 4);
		}
		// the offset for other instructions is in byte 2 of 'off' for
		// primitive types (buf == null)
		return opcode + (this.buf == null ? (this.off & 0xFF0000) >> 16 : 4);
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Type))
		{
			return false;
		}
		Type t = (Type) o;
		if (this.sort != t.sort)
		{
			return false;
		}
		if (this.sort >= ARRAY)
		{
			if (this.len != t.len)
			{
				return false;
			}
			for (int i = this.off, j = t.off, end = i + this.len; i < end; i++, j++)
			{
				if (this.buf[i] != t.buf[j])
				{
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		int hc = 13 * this.sort;
		if (this.sort >= ARRAY)
		{
			for (int i = this.off, end = i + this.len; i < end; i++)
			{
				hc = 17 * (hc + this.buf[i]);
			}
		}
		return hc;
	}
	
	@Override
	public String toString()
	{
		return this.getDescriptor();
	}
}
