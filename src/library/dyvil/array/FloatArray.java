package dyvil.array;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface FloatArray
{
	public static final float[]	EMPTY	= new float[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(float[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FALOAD })
	public static @infix float apply(float[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FASTORE })
	public static @infix void update(float[] array, int i, float v)
	{
		array[i] = v;
	}
	
	// Operators
	
	public static @infix float[] $plus(float[] a, float[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		float[] res = new float[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix String toString(float[] array)
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
	
	public static @infix int indexOf(float[] array, float v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(float[] array, float v, int start)
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
	
	public static @infix int lastIndexOf(float[] array, float v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(float[] array, float v, int start)
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
	
	public static @infix boolean contains(float[] array, float v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
