package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Byte;
import dyvil.lang.Int;
import dyvil.ref.ByteRef;
import dyvil.ref.array.ByteArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static dyvil.reflect.Opcodes.*;

public interface ByteArray
{
	byte[] EMPTY = new byte[0];
	
	static byte[] apply()
	{
		return EMPTY;
	}
	
	static byte[] apply(int count)
	{
		return new byte[count];
	}
	
	static byte[] repeat(int count, byte repeatedValue)
	{
		byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static byte[] generate(int count, IntUnaryOperator generator)
	{
		byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (byte) generator.applyAsInt(i);
		}
		return array;
	}
	
	static byte[] range(byte start, byte end)
	{
		int i = 0;
		byte[] array = new byte[end - start + 1];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static byte[] rangeOpen(byte start, byte end)
	{
		int i = 0;
		byte[] array = new byte[end - start];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static int length(byte[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, BALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static byte subscript(byte[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] subscript(byte[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		byte[] slice = new byte[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, BASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(byte[] array, int i, byte v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(byte[] array, Range<Int> range, byte[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static ByteRef subscriptRef(byte[] array, int index)
	{
		return new ByteArrayRef(array, index);
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	static boolean isEmpty(byte[] array)
	{
		return array.length == 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void forEach(byte[] array, IntConsumer action)
	{
		for (byte v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $qmark(byte[] array, byte v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $eq$eq(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $bang$eq(byte[] array1, byte[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] $plus(byte[] array, byte v)
	{
		int len = array.length;
		byte[] res = new byte[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] $plus$plus(byte[] array1, byte[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		byte[] res = new byte[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] $minus(byte[] array, byte v)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] $minus$minus(byte[] array1, byte[] array2)
	{
		int index = 0;
		int len = array1.length;
		byte[] res = new byte[len];
		
		for (byte v : array1)
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
	static byte[] $amp(byte[] array1, byte[] array2)
	{
		int index = 0;
		int len = array1.length;
		byte[] res = new byte[len];
		
		for (byte v : array1)
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
	static byte[] mapped(byte[] array, IntUnaryOperator mapper)
	{
		int len = array.length;
		byte[] res = new byte[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (byte) mapper.applyAsInt(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] flatMapped(byte[] array, IntFunction<byte[]> mapper)
	{
		int len = array.length;
		int size = 0;
		byte[] res = EMPTY;
		
		for (byte v : array)
		{
			byte[] a = mapper.apply(v);
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] filtered(byte[] array, IntPredicate condition)
	{
		int index = 0;
		int len = array.length;
		byte[] res = new byte[len];
		for (byte v : array)
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
	static byte[] sorted(byte[] array)
	{
		byte[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(byte[] array, byte v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(byte[] array, byte v, int start)
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
	static int lastIndexOf(byte[] array, byte v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(byte[] array, byte v, int start)
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
	static boolean contains(byte[] array, byte v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean in(byte v, byte[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static byte[] copy(byte[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Byte[] boxed(byte[] array)
	{
		int len = array.length;
		Byte[] boxed = new Byte[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Byte.apply(array[i]);
		}
		return boxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Iterable<Byte> toIterable(byte[] array)
	{
		return new ArrayList<Byte>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean equals(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int hashCode(byte[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String toString(byte[] array)
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
	static void toString(byte[] array, StringBuilder builder)
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
