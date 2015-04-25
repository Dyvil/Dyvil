package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface IntArray
{
	public static final int[]	EMPTY	= new int[0];
	
	public static int[] apply()
	{
		return EMPTY;
	}
	
	public static int[] apply(int count)
	{
		return new int[count];
	}
	
	public static int[] apply(int count, int repeatedValue)
	{
		int[] array = new int[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static int[] apply(int count, IntUnaryOperator generator)
	{
		int[] array = new int[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.applyAsInt(i);
		}
		return array;
	}
	
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
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	public static @infix void forEach(int[] array, IntConsumer action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix int[] $plus(int[] array, int v)
	{
		int len = array.length;
		int[] res = new int[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix int[] $plus$plus(int[] array1, int[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		int[] res = new int[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix int[] $minus(int[] array, int v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		int[] res = new int[len - 1];
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
	
	public static @infix int[] $minus$minus(int[] array1, int[] array2)
	{
		int index = 0;
		int len = array1.length;
		int[] res = new int[len];
		
		for (int i = 0; i < len; i++)
		{
			int v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix int[] $amp(int[] array1, int[] array2)
	{
		int index = 0;
		int len = array1.length;
		int[] res = new int[len];
		
		for (int i = 0; i < len; i++)
		{
			int v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix int[] mapped(int[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		int[] res = new int[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	public static @infix int[] filtered(int[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		int[] res = new int[len];
		for (int i = 0; i < len; i++)
		{
			int v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix int[] sorted(int[] array)
	{
		int[] res = array.clone();
		Arrays.sort(res);
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
	
	public static @infix void toString(int[] array, StringBuilder builder)
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
