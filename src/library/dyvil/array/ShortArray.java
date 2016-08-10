package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.ref.ShortRef;
import dyvil.ref.array.ShortArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class ShortArray
{
	public static final short[] EMPTY = new short[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static short[] apply()
	{
		return new short[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static short[] apply(int size)
	{
		return new short[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static short[] apply(short[] array)
	{
		return array.clone();
	}

	public static short[] apply(int size, short repeatedValue)
	{
		final short[] array = new short[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static short[] apply(int size, IntSupplier valueSupplier)
	{
		final short[] array = new short[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (short) valueSupplier.getAsInt();
		}
		return array;
	}

	public static short[] apply(int size, IntUnaryOperator valueMapper)
	{
		final short[] array = new short[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (short) valueMapper.applyAsInt(i);
		}
		return array;
	}

	public static short[] apply(short from, short to)
	{
		int i = 0;
		final short[] array = new short[to - from + 1];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	public static short[] apply_$_rangeOpen(short from, short toExclusive)
	{
		int i = 0;
		final short[] array = new short[toExclusive - from];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(short[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(short[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(short[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, SALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static short subscript(short[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] subscript(short[] array, Range<@Primitive Integer> range)
	{
		final int start = (range.first());
		final int count = range.count();
		final short[] slice = new short[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, SASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(short[] array, int index, short newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(short[] array, Range<@Primitive Integer> range, short[] newValues)
	{
		final int start = (range.first());
		final int count = range.count();
		System.arraycopy(newValues, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static ShortRef subscript_$amp(short[] array, int index)
	{
		return new ShortArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(short[] array, IntConsumer action)
	{
		for (short value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(short[] array, short value)
	{
		return Arrays.binarySearch(array, value) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(short[] array1, short[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(short[] array1, short[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] $plus(short[] array, short value)
	{
		final int len = array.length;
		final short[] res = new short[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] $plus$plus(short[] array1, short[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final short[] res = new short[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] $minus(short[] array, short value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final short[] res = new short[len - 1];
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
	public static short[] $minus$minus(short[] array1, short[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final short[] res = new short[len];

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

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] $amp(short[] array1, short[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final short[] res = new short[len];

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

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] mapped(short[] array, IntUnaryOperator mapper)
	{
		final int len = array.length;
		final short[] res = new short[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (short) mapper.applyAsInt(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] flatMapped(short[] array, IntFunction<short[]> mapper)
	{
		int size = 0;
		short[] res = EMPTY;

		for (short v : array)
		{
			final short[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final short[] newRes = new short[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] filtered(short[] array, IntPredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final short[] res = new short[len];
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

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] sorted(short[] array)
	{
		final short[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(short[] array, short value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(short[] array, short value, int startIndex)
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
	public static int lastIndexOf(short[] array, short value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(short[] array, short value, int startIndex)
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
	public static boolean contains(short[] array, short value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(short value, short[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX)
	public static short[] copy(short[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Short[] boxed(short[] array)
	{
		final int len = array.length;
		final Short[] boxed = new Short[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = (array[i]);
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Iterable<Short> toIterable(short[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(short[] array1, short[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(short[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(short[] array)
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
	public static void toString(short[] array, StringBuilder builder)
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
