package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface LongArray
{
	public static final long[]	EMPTY	= new long[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(long[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LALOAD })
	public static @infix long apply(long[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LASTORE })
	public static @infix void update(long[] array, int i, long v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix long[] $plus(long[] a, long[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		long[] res = new long[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String toString(long[] array)
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
	
	public static @infix int indexOf(long[] array, long v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(long[] array, long v, int start)
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
	
	public static @infix int lastIndexOf(long[] array, long v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(long[] array, long v, int start)
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
	
	public static @infix boolean contains(long[] array, long v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
