package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Boolean;
import dyvil.lang.Int;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import static dyvil.reflect.Opcodes.*;

public interface BooleanArray
{
	boolean[] EMPTY = new boolean[0];
	
	static boolean[] apply()
	{
		return EMPTY;
	}
	
	static boolean[] apply(int count)
	{
		return new boolean[count];
	}
	
	static boolean[] repeat(int count, boolean repeatedValue)
	{
		boolean[] array = new boolean[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static boolean[] generate(int count, IntPredicate generator)
	{
		boolean[] array = new boolean[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.test(i);
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, LOAD_1, ARRAYLENGTH })
	static
	@infix
	int length(boolean[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, BALOAD })
	static
	@infix
	boolean subscript(boolean[] array, int i)
	{
		return array[i];
	}
	
	static
	@infix
	boolean[] subscript(boolean[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		boolean[] slice = new boolean[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, BASTORE })
	static
	@infix
	void subscript_$eq(boolean[] array, int i, boolean v)
	{
		array[i] = v;
	}
	
	static
	@infix
	void subscript_$eq(boolean[] array, Range<Int> range, boolean[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, ARRAYLENGTH, IFEQ })
	static
	@infix
	boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	static
	@infix
	void forEach(boolean[] array, Consumer<Boolean> action)
	{
		for (boolean v : array)
		{
			action.accept(Boolean.apply(v));
		}
	}
	
	// Operators
	
	static
	@infix
	@inline
	boolean $qmark(boolean[] array, boolean v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	static
	@infix
	@inline
	boolean $eq$eq(boolean[] array1, boolean[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static
	@infix
	@inline
	boolean $bang$eq(boolean[] array1, boolean[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	static
	@infix
	boolean[] $plus(boolean[] array, boolean v)
	{
		int len = array.length;
		boolean[] res = new boolean[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	static
	@infix
	boolean[] $plus$plus(boolean[] array1, boolean[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		boolean[] res = new boolean[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	static
	@infix
	boolean[] $minus(boolean[] array, boolean v)
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
	
	static
	@infix
	boolean[] $minus$minus(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		int len = array1.length;
		boolean[] res = new boolean[len];
		
		for (boolean v : array1)
		{
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static
	@infix
	boolean[] $amp(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		int len = array1.length;
		boolean[] res = new boolean[len];
		
		for (boolean v : array1)
		{
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static
	@infix
	boolean[] mapped(boolean[] array, Predicate<Boolean> mapper)
	{
		int len = array.length;
		boolean[] res = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.test(Boolean.apply(array[i]));
		}
		return res;
	}
	
	static
	@infix
	boolean[] flatMapped(boolean[] array, Function<Boolean, boolean[]> mapper)
	{
		int size = 0;
		boolean[] res = EMPTY;
		
		for (boolean v : array)
		{
			boolean[] a = mapper.apply(Boolean.apply(v));
			int alen = a.length;
			if (size + alen >= res.length)
			{
				boolean[] newRes = new boolean[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	static
	@infix
	boolean[] filtered(boolean[] array, Predicate<Boolean> condition)
	{
		int index = 0;
		int len = array.length;
		boolean[] res = new boolean[len];
		for (boolean v : array)
		{
			if (condition.test(Boolean.apply(v)))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static
	@infix
	boolean[] sorted(boolean[] array)
	{
		int len = array.length;
		if (len <= 0)
		{
			return array;
		}
		
		boolean[] res = new boolean[len];
		
		// Count the number of 'false' in the array
		int falseEntries = 0;
		
		for (boolean v : array)
		{
			if (!v)
			{
				falseEntries++;
			}
		}
		
		// Make the remaining elements of the result true
		for (; falseEntries < len; falseEntries++)
		{
			res[falseEntries] = true;
		}
		return res;
	}
	
	// Search Operations
	
	static
	@infix
	int indexOf(boolean[] array, boolean v)
	{
		return indexOf(array, v, 0);
	}
	
	static
	@infix
	int indexOf(boolean[] array, boolean v, int start)
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
	
	static
	@infix
	int lastIndexOf(boolean[] array, boolean v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	static
	@infix
	int lastIndexOf(boolean[] array, boolean v, int start)
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
	
	static
	@infix
	@inline
	boolean contains(boolean[] array, boolean v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	static
	@infix
	@inline
	boolean in(boolean v, boolean[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	static
	@infix
	@inline
	boolean[] copy(boolean[] array)
	{
		return array.clone();
	}
	
	static
	@infix
	Boolean[] boxed(boolean[] array)
	{
		int len = array.length;
		Boolean[] boxed = new Boolean[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Boolean.apply(array[i]);
		}
		return boxed;
	}
	
	static
	@infix
	Iterable<Boolean> toIterable(boolean[] array)
	{
		return new ArrayList<Boolean>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	static
	@infix
	@inline
	boolean equals(boolean[] array1, boolean[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static
	@infix
	@inline
	int hashCode(boolean[] array)
	{
		return Arrays.hashCode(array);
	}
	
	static
	@infix
	String toString(boolean[] array)
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
	
	static
	@infix
	void toString(boolean[] array, StringBuilder builder)
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
