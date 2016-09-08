package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.range.closed.LongRange;
import dyvil.ref.LongRef;
import dyvil.ref.array.LongArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class LongArray
{
	public static final long[] EMPTY = new long[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static long[] apply()
	{
		return new long[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static long[] apply(int size)
	{
		return new long[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static long[] apply(long[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.IMPLICIT | Modifiers.INLINE)
	public static long[] apply(LongRange range)
	{
		return range.toLongArray();
	}

	public static long[] apply(int size, long repeatedValue)
	{
		final long[] array = new long[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static long[] apply(int size, LongSupplier valueSupplier)
	{
		final long[] array = new long[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueSupplier.getAsLong();
		}
		return array;
	}

	public static long[] apply(int size, LongUnaryOperator valueMapper)
	{
		final long[] array = new long[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueMapper.applyAsLong(i);
		}
		return array;
	}

	public static long[] apply_$_closed(long from, long to)
	{
		int i = 0;
		final long[] array = new long[(int) (to - from + 1)];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	public static long[] apply_$_halfOpen(long from, long toExclusive)
	{
		int i = 0;
		final long[] array = new long[(int) (toExclusive - from)];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(long[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(long[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(long[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static long subscript(long[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] subscript(long[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final long[] result = new long[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, LASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(long[] array, int index, long newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(long[] array, Range<@Primitive Integer> range, long[] newValues)
	{
		System.arraycopy(newValues, 0, array, range.first(), range.size());
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static LongRef subscript_$amp(long[] array, int index)
	{
		return new LongArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(long[] array, LongConsumer action)
	{
		for (long value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(long[] array, long value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(long[] array1, long[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(long[] array1, long[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] $plus(long[] array, long v)
	{
		final int len = array.length;
		final long[] res = new long[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] $plus$plus(long[] array1, long[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final long[] res = new long[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] $minus(long[] array, long v)
	{
		final int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final long[] res = new long[len - 1];
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
	public static long[] $minus$minus(long[] array1, long[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final long[] res = new long[len];

		for (long v : array1)
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
	public static long[] $amp(long[] array1, long[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final long[] res = new long[len];

		for (long v : array1)
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
	public static long[] mapped(long[] array, LongUnaryOperator mapper)
	{
		final int len = array.length;
		final long[] res = new long[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsLong(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] flatMapped(long[] array, LongFunction<long[]> mapper)
	{
		int size = 0;
		long[] res = EMPTY;

		for (long v : array)
		{
			final long[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final long[] newRes = new long[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long[] filtered(long[] array, LongPredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final long[] res = new long[len];
		for (long v : array)
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
	public static long[] sorted(long[] array)
	{
		final long[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(long[] array, long value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(long[] array, long value, int startIndex)
	{
		for (; startIndex < array.length; startIndex++)
		{
			if (array[startIndex] == value)
			{
				return startIndex;
			}
		}
		return -1;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(long[] array, long value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(long[] array, long value, int startIndex)
	{
		for (; startIndex >= 0; startIndex--)
		{
			if (array[startIndex] == value)
			{
				return startIndex;
			}
		}
		return -1;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean contains(long[] array, long value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(long value, long[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static long[] copy(long[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Long[] boxed(long[] array)
	{
		final int len = array.length;
		final Long[] boxed = new Long[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Iterable<Long> toIterable(long[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(long[] array1, long[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(long[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(long[] array)
	{
		if (array == null)
		{
			return "null";
		}

		final int len = array.length;
		if (len <= 0)
		{
			return "[]";
		}

		final StringBuilder buf = new StringBuilder(len * 3 + 4);
		buf.append('[').append(array[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(array[i]);
		}
		return buf.append(']').toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void toString(long[] array, StringBuilder builder)
	{
		if (array == null)
		{
			builder.append("null");
			return;
		}

		final int len = array.length;
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
