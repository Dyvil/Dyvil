package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Char;
import dyvil.lang.Int;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static dyvil.reflect.Opcodes.*;

public interface CharArray
{
	char[] EMPTY = new char[0];
	
	static char[] apply()
	{
		return EMPTY;
	}
	
	static char[] apply(int count)
	{
		return new char[count];
	}
	
	static char[] repeat(int count, char repeatedValue)
	{
		char[] array = new char[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static char[] generate(int count, IntUnaryOperator generator)
	{
		char[] array = new char[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (char) generator.applyAsInt(i);
		}
		return array;
	}
	
	static char[] range(char start, char end)
	{
		int i = 0;
		char[] array = new char[end - start + 1];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static char[] rangeOpen(char start, char end)
	{
		int i = 0;
		char[] array = new char[end - start];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static int length(char[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, CALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static char subscript(char[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] subscript(char[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		char[] slice = new char[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, CASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(char[] array, int i, char v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(char[] array, Range<Int> range, char[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	static boolean isEmpty(char[] array)
	{
		return array.length == 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void forEach(char[] array, IntConsumer action)
	{
		for (char v : array)
		{
			action.accept(v);
		}
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $qmark(char[] array, char v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $eq$eq(char[] array1, char[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $bang$eq(char[] array1, char[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	// Operators
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] $plus(char[] array, char v)
	{
		int len = array.length;
		char[] res = new char[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] $plus$plus(char[] array1, char[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		char[] res = new char[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] $minus(char[] array, char v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		char[] res = new char[len - 1];
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
	static char[] $minus$minus(char[] array1, char[] array2)
	{
		int index = 0;
		int len = array1.length;
		char[] res = new char[len];
		
		for (char v : array1)
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
	static char[] $amp(char[] array1, char[] array2)
	{
		int index = 0;
		int len = array1.length;
		char[] res = new char[len];
		
		for (char v : array1)
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
	static char[] mapped(char[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		char[] res = new char[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (char) mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] flatMapped(char[] array, IntFunction<char[]> mapper)
	{
		int size = 0;
		char[] res = EMPTY;
		
		for (char v : array)
		{
			char[] a = mapper.apply(v);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				char[] newRes = new char[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] filtered(char[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		char[] res = new char[len];
		for (char v : array)
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
	static char[] sorted(char[] array)
	{
		char[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(char[] array, char v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(char[] array, char v, int start)
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
	static int lastIndexOf(char[] array, char v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(char[] array, char v, int start)
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
	static boolean contains(char[] array, char v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean in(char v, char[] array)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static char[] copy(char[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Char[] boxed(char[] array)
	{
		int len = array.length;
		Char[] boxed = new Char[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Char.apply(array[i]);
		}
		return boxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Iterable<Char> toIterable(char[] array)
	{
		return new ArrayList<Char>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean equals(char[] array1, char[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int hashCode(char[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String asString(char[] a)
	{
		return new String(a);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String toString(char[] a)
	{
		if (a == null)
		{
			return "null";
		}
		
		int len = a.length;
		if (len <= 0)
		{
			return "[]";
		}
		
		StringBuilder buf = new StringBuilder(len * 3 + 4);
		buf.append('[').append(a[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(a[i]);
		}
		return buf.append(']').toString();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void toString(char[] array, StringBuilder builder)
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
