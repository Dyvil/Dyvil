package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.lang.reflect.Array;
import java.util.Objects;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface ObjectArray
{
	public static final Object[]	EMPTY	= new Object[0];
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix <T> int length(T[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AALOAD })
	public static @infix <T> T apply(T[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AASTORE })
	public static @infix <T> void update(T[] array, int i, T v)
	{
		array[i] = v;
	}
	
	// Array Creation
	
	public static @infix <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	// Component Types
	
	public static @infix <T> Class<T> getComponentType(T[] array)
	{
		return (Class<T>) array.getClass().getComponentType();
	}
	
	public static @infix <T> Class getDeepComponentType(T[] array)
	{
		Class ret = array.getClass();
		while (true)
		{
			Class c = ret.getComponentType();
			if (c == null)
			{
				return ret;
			}
			ret = c;
		}
	}
	
	// Operators
	
	public static @infix <T> T[] $plus(T[] a, T[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		T[] res = (T[]) new Object[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix <T> String toString(T[] array)
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
	
	public static @infix <T> int indexOf(T[] array, T v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix <T> int indexOf(T[] array, T v, int start)
	{
		for (; start < array.length; start++)
		{
			if (Objects.equals(v, array[start]))
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix <T> int lastIndexOf(T[] array, T v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix <T> int lastIndexOf(T[] array, T v, int start)
	{
		for (; start >= 0; start--)
		{
			if (Objects.equals(v, array[start]))
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix <T> boolean contains(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	// Copying
	
	public static @infix <T> T[] copy(T[] array)
	{
		return array.clone();
	}
	
	public static @infix <T> T[] copy(T[] array, int newLength)
	{
		return (T[]) java.util.Arrays.copyOf(array, newLength, array.getClass());
	}
	
	public static @infix <T, N> N[] copy(T[] array, int newLength, Class<? extends N[]> newType)
	{
		return java.util.Arrays.<N, T> copyOf(array, newLength, newType);
	}
}
