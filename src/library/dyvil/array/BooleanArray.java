package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface BooleanArray
{
	public static final boolean[]	EMPTY	= new boolean[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(boolean[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BALOAD })
	public static @infix boolean apply(boolean[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BASTORE })
	public static @infix void update(boolean[] array, int i, boolean v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix boolean[] $plus(boolean[] array1, boolean[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		boolean[] res = new boolean[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String toString(boolean[] array)
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
	
	public static @infix int indexOf(boolean[] array, boolean v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(boolean[] array, boolean v, int start)
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
	
	public static @infix int lastIndexOf(boolean[] array, boolean v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(boolean[] array, boolean v, int start)
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
	
	public static @infix boolean contains(boolean[] array, boolean v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
