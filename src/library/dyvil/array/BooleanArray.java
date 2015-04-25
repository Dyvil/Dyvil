package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.lang.Boolean;

public interface BooleanArray
{
	public static final boolean[]	EMPTY	= new boolean[0];
	
	public static boolean[] apply()
	{
		return EMPTY;
	}
	
	public static boolean[] apply(int count)
	{
		return new boolean[count];
	}
	
	public static boolean[] apply(int count, boolean repeatedValue)
	{
		boolean[] array = new boolean[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static boolean[] apply(int count, IntPredicate generator)
	{
		boolean[] array = new boolean[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.test(i);
		}
		return array;
	}
	
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
	
	public static @infix boolean[] $plus(boolean[] array, boolean v)
	{
		int len = array.length;
		boolean[] res = new boolean[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix boolean[] $plus$plus(boolean[] array1, boolean[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		boolean[] res = new boolean[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix boolean[] $minus(boolean[] array, boolean v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		boolean[] res = new boolean[len - 1];
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
	
	public static @infix boolean[] $minus$minus(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		int len = array1.length;
		boolean[] res = new boolean[len];
		
		for (int i = 0; i < len; i++)
		{
			boolean v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix boolean[] $amp(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		int len = array1.length;
		boolean[] res = new boolean[len];
		
		for (int i = 0; i < len; i++)
		{
			boolean v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix boolean[] mapped(boolean[] array, Predicate<Boolean> mapper)
	{
		int len = array.length;
		boolean[] res = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.test(Boolean.apply(array[i]));
		}
		return res;
	}
	
	public static @infix boolean[] filtered(boolean[] array, Predicate<Boolean> condition)
	{
		int index = 0;
		int len = array.length;
		boolean[] res = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			boolean v = array[i];
			if (condition.test(Boolean.apply(v)))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix boolean[] sorted(boolean[] array)
	{
		int len = array.length;
		if (len <= 0)
		{
			return array;
		}
		
		boolean[] res = new boolean[len];
		
		// Count the number of 'false' in the array
		int f = 0;
		
		for (int i = 0; i < len; i++)
		{
			if (!array[i])
			{
				f++;
			}
		}
		
		// Make the remaining elements of the result true
		for (; f < len; f++)
		{
			res[f] = true;
		}
		
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
	
	public static @infix void toString(boolean[] array, StringBuilder builder)
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
