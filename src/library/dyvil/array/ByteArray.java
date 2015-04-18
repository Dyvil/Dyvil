package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface ByteArray
{
	public static final byte[]	EMPTY	= new byte[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(byte[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BALOAD })
	public static @infix byte apply(byte[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BASTORE })
	public static @infix void update(byte[] array, int i, byte v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix byte[] $plus(byte[] array1, byte[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		byte[] res = new byte[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String toString(byte[] array)
	{
		if (array == null)
		{
			return "null";
		}
		
		int len = array.length;
		if (len <= 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder(len * 3 + 4);
		buf.append('[').append(array[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(array[i]);
		}
		return buf.append(']').toString();
	}
	
	// Search Operations
	
	public static @infix int indexOf(byte[] array, byte v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(byte[] array, byte v, int start)
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
	
	public static @infix int lastIndexOf(byte[] array, byte v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(byte[] array, byte v, int start)
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
	
	public static @infix boolean contains(byte[] array, byte v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
