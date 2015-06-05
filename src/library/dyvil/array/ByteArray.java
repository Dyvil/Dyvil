package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;

public interface ByteArray
{
	public static final byte[]	EMPTY	= new byte[0];
	
	public static byte[] apply()
	{
		return EMPTY;
	}
	
	public static byte[] apply(int count)
	{
		return new byte[count];
	}
	
	public static byte[] apply(int count, byte repeatedValue)
	{
		byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static byte[] apply(int count, IntUnaryOperator generator)
	{
		byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (byte) generator.applyAsInt(i);
		}
		return array;
	}
	
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
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(byte[] array)
	{
		return array.length == 0;
	}
	
	public static @infix void forEach(byte[] array, IntConsumer action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix @inline boolean $qmark(byte[] array, byte v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	public static @infix @inline boolean $eq$eq(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline boolean $bang$eq(byte[] array1, byte[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	public static @infix byte[] $plus(byte[] array, byte v)
	{
		int len = array.length;
		byte[] res = new byte[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix byte[] $plus$plus(byte[] array1, byte[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		byte[] res = new byte[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix byte[] $minus(byte[] array, byte v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		byte[] res = new byte[len - 1];
		if (index > 0)
		{
			// copy the first part before the index
			System.arraycopy(array, 0, res, 0, index);
		}
		if (index < len)
		{
			// copy the second part after the index
			System.arraycopy(array, index + 1, res, index, len - index - 1);
		}
		return res;
	}
	
	public static @infix byte[] $minus$minus(byte[] array1, byte[] array2)
	{
		int index = 0;
		int len = array1.length;
		byte[] res = new byte[len];
		
		for (int i = 0; i < len; i++)
		{
			byte v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix byte[] $amp(byte[] array1, byte[] array2)
	{
		int index = 0;
		int len = array1.length;
		byte[] res = new byte[len];
		
		for (int i = 0; i < len; i++)
		{
			byte v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix byte[] mapped(byte[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (byte) mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	public static @infix byte[] flatMapped(byte[] array, IntFunction<byte[]> mapper)
	{
		int len = array.length;
		int size = 0;
		byte[] res = EMPTY;
		
		for (int i = 0; i < len; i++)
		{
			byte[] a = mapper.apply(array[i]);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				byte[] newRes = new byte[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	public static @infix byte[] filtered(byte[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++)
		{
			byte v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix byte[] sorted(byte[] array)
	{
		byte[] res = array.clone();
		Arrays.sort(res);
		return res;
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
	
	public static @infix @inline boolean contains(byte[] array, byte v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	public static @infix @inline boolean in(byte v, byte[] array)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	// Copying
	
	public static @infix @inline byte[] copy(byte[] array)
	{
		return array.clone();
	}
	
	// equals, hashCode and toString
	
	public static @infix @inline boolean equals(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline int hashCode(byte[] array)
	{
		return Arrays.hashCode(array);
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
	
	public static @infix void toString(byte[] array, StringBuilder builder)
	{
		if (array == null)
		{
			builder.append("null");
			return;
		}
		
		int len = array.length;
		if (len <= 0)
		{
			builder.append("[]");
			return;
		}
		
		builder.append('[').append(array[0]);
		for (int i = 1; i < len; i++)
		{
			builder.append(", ");
			builder.append(array[i]);
		}
		builder.append(']');
	}
}
