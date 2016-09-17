package dyvil.array;

import dyvil.annotation.Immutable;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.DyvilName;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.ImmutableList;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.range.closed.IntRange;
import dyvil.ref.IntRef;
import dyvil.ref.array.IntArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class IntArray
{
	public static final int[] EMPTY = new int[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static int[] apply()
	{
		return new int[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static int[] apply(int size)
	{
		return new int[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static int[] apply(int[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.IMPLICIT | Modifiers.INLINE)
	public static int[] apply(IntRange range)
	{
		return range.toIntArray();
	}

	public static int[] apply(int size, int repeatedValue)
	{
		final int[] array = new int[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static int[] apply(int size, IntSupplier valueSupplier)
	{
		final int[] array = new int[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueSupplier.getAsInt();
		}
		return array;
	}

	public static int[] apply(int size, IntUnaryOperator valueMapper)
	{
		final int[] array = new int[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueMapper.applyAsInt(i);
		}
		return array;
	}

	@DyvilName("apply")
	public static int[] rangeClosed(int from, int to)
	{
		int i = 0;
		final int[] array = new int[to - from + 1];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	@DyvilName("apply")
	public static int[] range(int from, int toExclusive)
	{
		int i = 0;
		final int[] array = new int[toExclusive - from];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(int[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(int[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, IALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static int subscript(int[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] subscript(int[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final int[] result = new int[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, IASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(int[] array, int index, int newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(int[] array, Range<@Primitive Integer> range, int[] newValues)
	{
		final int size = range.size();
		System.arraycopy(newValues, 0, array, range.first(), size);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static IntRef subscript_$amp(int[] array, int index)
	{
		return new IntArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(int[] array, IntConsumer action)
	{
		for (int value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(int[] array, int value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(int[] array1, int[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(int[] array1, int[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] $plus(int[] array, int value)
	{
		final int len = array.length;
		final int[] res = new int[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] $plus$plus(int[] array1, int[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final int[] res = new int[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] $minus(int[] array, int value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final int[] res = new int[len - 1];
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
	public static int[] $minus$minus(int[] array1, int[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final int[] res = new int[len];

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
	public static int[] $amp(int[] array1, int[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final int[] res = new int[len];

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
	public static int[] mapped(int[] array, IntUnaryOperator mapper)
	{
		final int len = array.length;
		final int[] res = new int[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsInt(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] flatMapped(int[] array, IntFunction<int[]> mapper)
	{
		int size = 0;
		int[] res = EMPTY;

		for (int v : array)
		{
			final int[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final int[] newRes = new int[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int[] filtered(int[] array, IntPredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final int[] res = new int[len];
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
	public static int[] sorted(int[] array)
	{
		final int[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(int[] array, int value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(int[] array, int value, int startIndex)
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
	public static int lastIndexOf(int[] array, int value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(int[] array, int value, int startIndex)
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
	public static boolean contains(int[] array, int value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(int value, int[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int[] copy(int[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Integer[] boxed(int[] array)
	{
		final int len = array.length;
		final Integer[] boxed = new Integer[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static Iterable<@Primitive Integer> asIterable(int[] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static ImmutableList<@Primitive Integer> asList(int @Immutable [] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static ImmutableList<@Primitive Integer> toList(int[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(int[] array1, int[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(int[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(int[] array)
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
	public static void toString(int[] array, StringBuilder builder)
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
