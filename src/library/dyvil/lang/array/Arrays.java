package dyvil.lang.array;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import dyvil.collections.Collections;
import dyvil.lang.annotation.infix;
import dyvil.util.ArrayUtils;

/**
 * The Arrays class provides default methods for creating, copying, converting
 * and searching all kinds of arrays.
 * 
 * @see ArrayUtils
 * @author Clashsoft
 */
public class Arrays
{
	public static @infix <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	// Array Copies
	
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
	
	// Collection Conversions
	
	public static @infix <T> List<T> toList(T... array)
	{
		return java.util.Arrays.asList(array);
	}
	
	public static @infix <T> T[] toArray(Collection<? extends T> collection)
	{
		return Collections.toArray(collection);
	}
	
	public static @infix <T> T[] toArray(Collection<? extends T> collection, Class<T> type)
	{
		return Collections.toArray(collection, type);
	}
	
	// Generic array functions
	
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
	
	// int array functions
	
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
	
	// long array functions
	
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
	
	// float array functions
	
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
	
	// double array functions
	
	public static @infix int indexOf(double[] array, double v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(double[] array, double v, int start)
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
	
	public static @infix int lastIndexOf(double[] array, double v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(double[] array, double v, int start)
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
	
	public static @infix boolean contains(double[] array, double v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
