package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface IntArray
{
	public static final int[]		EMPTY		= new int[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(int[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IALOAD })
	public static @infix int apply(int[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IASTORE })
	public static @infix void update(int[] array, int i, int v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix int[] $plus(int[] a, int[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		int[] res = new int[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String toString(int[] array)
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
	
	public static @infix int indexOf(int[] array, int v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(int[] array, int v, int start)
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
	
	public static @infix int lastIndexOf(int[] array, int v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(int[] array, int v, int start)
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
	
	public static @infix boolean contains(int[] array, int v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
