package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Int;
import dyvil.lang.Short;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static dyvil.reflect.Opcodes.*;

public interface ShortArray
{
	short[] EMPTY = new short[0];
	
	static short[] apply()
	{
		return EMPTY;
	}
	
	static short[] apply(int count)
	{
		return new short[count];
	}
	
	static short[] repeat(int count, short repeatedValue)
	{
		short[] array = new short[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static short[] generate(int count, IntUnaryOperator generator)
	{
		short[] array = new short[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (short) generator.applyAsInt(i);
		}
		return array;
	}
	
	static short[] apply(short start, short end)
	{
		int i = 0;
		short[] array = new short[end - start + 1];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static short[] range(short start, short end)
	{
		int i = 0;
		short[] array = new short[end - start + 1];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static short[] rangeOpen(short start, short end)
	{
		int i = 0;
		short[] array = new short[end - start];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH })
	static @infix int length(short[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, SALOAD })
	static @infix short subscript(short[] array, int i)
	{
		return array[i];
	}
	
	static @infix short[] subscript(short[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		short[] slice = new short[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, SASTORE })
	static @infix void subscript_$eq(short[] array, int i, short v)
	{
		array[i] = v;
	}
	
	static @infix void subscript_$eq(short[] array, Range<Int> range, short[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH, IFEQ })
	static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	static @infix void forEach(int[] array, IntConsumer action)
	{
		for (int v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	static @infix @inline boolean $qmark(short[] array, short v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	static @infix @inline boolean $eq$eq(short[] array1, short[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static @infix @inline boolean $bang$eq(short[] array1, short[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	static @infix short[] $plus(short[] array, short v)
	{
		int len = array.length;
		short[] res = new short[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	static @infix short[] $plus$plus(short[] array1, short[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		short[] res = new short[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	static @infix short[] $minus(short[] array, short v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		short[] res = new short[len - 1];
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
	
	static @infix short[] $minus$minus(short[] array1, short[] array2)
	{
		int index = 0;
		int len = array1.length;
		short[] res = new short[len];
		
		for (short v : array1)
		{
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix short[] $amp(short[] array1, short[] array2)
	{
		int index = 0;
		int len = array1.length;
		short[] res = new short[len];
		
		for (short v : array1)
		{
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix short[] mapped(short[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		short[] res = new short[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (short) mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	static @infix short[] flatMapped(short[] array, IntFunction<short[]> mapper)
	{
		int len = array.length;
		int size = 0;
		short[] res = EMPTY;
		
		for (short v : array)
		{
			short[] a = mapper.apply(v);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				short[] newRes = new short[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	static @infix short[] filtered(short[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		short[] res = new short[len];
		for (short v : array)
		{
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix short[] sorted(short[] array)
	{
		short[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	static @infix int indexOf(short[] array, short v)
	{
		return indexOf(array, v, 0);
	}
	
	static @infix int indexOf(short[] array, short v, int start)
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
	
	static @infix int lastIndexOf(short[] array, short v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	static @infix int lastIndexOf(short[] array, short v, int start)
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
	
	static @infix @inline boolean contains(short[] array, short v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	static @infix @inline boolean in(short v, short[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	static @infix short[] copy(short[] array)
	{
		return array.clone();
	}
	
	static @infix Short[] boxed(short[] array)
	{
		int len = array.length;
		Short[] boxed = new Short[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Short.apply(array[i]);
		}
		return boxed;
	}
	
	static @infix Iterable<Short> toIterable(short[] array)
	{
		return new ArrayList<Short>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	static @infix @inline boolean equals(short[] array1, short[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static @infix @inline int hashCode(short[] array)
	{
		return Arrays.hashCode(array);
	}
	
	static @infix String toString(short[] array)
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
	
	static @infix void toString(short[] array, StringBuilder builder)
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
