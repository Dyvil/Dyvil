package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface CharArray
{
	public static final char[]	EMPTY	= new char[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(char[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CALOAD })
	public static @infix char apply(char[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, CASTORE })
	public static @infix void update(char[] array, int i, char v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix char[] $plus(char[] a, char[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		char[] res = new char[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String asString(char[] a)
	{
		return new String(a);
	}
	
	public static @infix String toString(char[] a)
	{
		if (a == null)
		{
			return "null";
		}
		
		int len = a.length;
		if (len <= 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder(len * 3 + 4);
		buf.append('[').append(a[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(a[i]);
		}
		return buf.append(']').toString();
	}
	
	// Search Operations
	
	public static @infix int indexOf(char[] array, char v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(char[] array, char v, int start)
	{
		for (; start < array.length; start++)
		{
			if (array[start] == v)
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix int lastIndexOf(char[] array, char v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(char[] array, char v, int start)
	{
		for (; start >= 0; start--)
		{
			if (array[start] == v)
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix boolean contains(char[] array, char v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
