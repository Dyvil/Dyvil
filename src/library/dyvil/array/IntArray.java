package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.range.IntRange;
import dyvil.ref.IntRef;
import dyvil.ref.array.IntArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static dyvil.reflect.Opcodes.*;

public interface IntArray
{
	int[] EMPTY = new int[0];
	
	static int[] apply()
	{
		return EMPTY;
	}
	
	static int[] apply(int count)
	{
		return new int[count];
	}

	@DyvilModifiers(Modifiers.IMPLICIT | Modifiers.INLINE)
	static int[] of(IntRange range)
	{
		return range.toIntArray();
	}
	
	static int[] repeat(int count, int repeatedValue)
	{
		int[] array = new int[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static int[] generate(int count, IntUnaryOperator generator)
	{
		int[] array = new int[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.applyAsInt(i);
		}
		return array;
	}
	
	static int[] range(int start, int end)
	{
		int i = 0;
		int[] array = new int[end - start + 1];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static int[] rangeOpen(int start, int end)
	{
		int i = 0;
		int[] array = new int[end - start];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static int length(int[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, IALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static int subscript(int[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] subscript(int[] array, Range<Integer> range)
	{
		int start = (range.first());
		int count = range.count();
		int[] slice = new int[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, IASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(int[] array, int i, int v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(int[] array, Range<Integer> range, int[] values)
	{
		int start = (range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static IntRef subscript_$amp(int[] array, int index)
	{
		return new IntArrayRef(array, index);
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	static boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void forEach(int[] array, IntConsumer action)
	{
		for (int v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $qmark(int[] array, int v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $eq$eq(int[] array1, int[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $bang$eq(int[] array1, int[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] $plus(int[] array, int v)
	{
		int len = array.length;
		int[] res = new int[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] $plus$plus(int[] array1, int[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		int[] res = new int[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] $minus(int[] array, int v)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] $minus$minus(int[] array1, int[] array2)
	{
		int index = 0;
		int len = array1.length;
		int[] res = new int[len];
		
		for (int v : array1)
		{
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] $amp(int[] array1, int[] array2)
	{
		int index = 0;
		int len = array1.length;
		int[] res = new int[len];
		
		for (int v : array1)
		{
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] mapped(int[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		int[] res = new int[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] flatMapped(int[] array, IntFunction<int[]> mapper)
	{
		int size = 0;
		int[] res = EMPTY;
		
		for (int v : array)
		{
			int[] a = mapper.apply(v);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				int[] newRes = new int[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] filtered(int[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		int[] res = new int[len];
		for (int v : array)
		{
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] sorted(int[] array)
	{
		int[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(int[] array, int v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(int[] array, int v, int start)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(int[] array, int v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(int[] array, int v, int start)
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
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean contains(int[] array, int v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean in(int v, int[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int[] copy(int[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Integer[] boxed(int[] array)
	{
		int len = array.length;
		Integer[] boxed = new Integer[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = (array[i]);
		}
		return boxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Iterable<Integer> toIterable(int[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean equals(int[] array1, int[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int hashCode(int[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String toString(int[] array)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static void toString(int[] array, StringBuilder builder)
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
