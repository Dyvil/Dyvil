package dyvil.arrays;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import dyvil.collections.CollectionUtils;
import dyvil.lang.annotation.Utility;
import dyvil.lang.annotation.infix;

/**
 * The {@linkplain Utility utility interface} <b>ArrayUtils</b> can be used for
 * several array-related operations such as concatenating, copying the array,
 * finding the index of an element or getting the component type of the array.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility(Object[].class)
public interface ArrayUtils
{
	public static final byte[]		EMPTY_BYTE_ARRAY	= new byte[0];
	public static final short[]		EMPTY_SHORT_ARRAY	= new short[0];
	public static final char[]		EMPTY_CHAR_ARRAY	= new char[0];
	public static final int[]		EMPTY_INT_ARRAY		= new int[0];
	public static final long[]		EMPTY_LONG_ARRAY	= new long[0];
	public static final float[]		EMPTY_FLOAT_ARRAY	= new float[0];
	public static final double[]	EMPTY_DOUBLE_ARRAY	= new double[0];
	public static final Object[]	EMPTY_OBJECT_ARRAY	= new Object[0];
	public static final String[]	EMPTY_STRING_ARRAY	= new String[0];
	
	public static @infix <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	// Concatenation
	
	public static @infix byte[] $plus(byte[] a, byte[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		byte[] res = new byte[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix short[] $plus(short[] a, short[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		short[] res = new short[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix char[] $plus(char[] a, char[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		char[] res = new char[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix int[] $plus(int[] a, int[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		int[] res = new int[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix long[] $plus(long[] a, long[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		long[] res = new long[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix float[] $plus(float[] a, float[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		float[] res = new float[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix double[] $plus(double[] a, double[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		double[] res = new double[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix <T> T[] $plus(T[] a, T[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		T[] res = (T[]) new Object[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
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
		return CollectionUtils.toArray(collection);
	}
	
	public static @infix <T> T[] toArray(Collection<? extends T> collection, Class<T> type)
	{
		return CollectionUtils.toArray(collection, type);
	}
	
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

	public static @infix boolean contains(int[] array, int v)
	{
		return indexOf(array, v, 0) != -1;
	}

	public static @infix boolean contains(long[] array, long v)
	{
		return indexOf(array, v, 0) != -1;
	}

	public static @infix boolean contains(float[] array, float v)
	{
		return indexOf(array, v, 0) != -1;
	}

	public static @infix boolean contains(double[] array, double v)
	{
		return indexOf(array, v, 0) != -1;
	}

	public static @infix <T> boolean contains(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
