package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.ref.ByteRef;
import dyvil.ref.array.ByteArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class ByteArray
{
	public static final byte[] EMPTY = new byte[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static byte[] apply()
	{
		return new byte[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static byte[] apply(int size)
	{
		return new byte[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static byte[] apply(byte[] array)
	{
		return array.clone();
	}

	public static byte[] apply(int size, byte repeatedValue)
	{
		final byte[] array = new byte[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static byte[] apply(int count, IntSupplier valueSupplier)
	{
		final byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (byte) valueSupplier.getAsInt();
		}
		return array;
	}

	public static byte[] apply(int count, IntUnaryOperator valueMapper)
	{
		final byte[] array = new byte[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (byte) valueMapper.applyAsInt(i);
		}
		return array;
	}

	public static byte[] apply(byte from, byte to)
	{
		int i = 0;
		final byte[] array = new byte[to - from + 1];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	public static byte[] apply_$_halfOpen(byte from, byte toExclusive)
	{
		int i = 0;
		final byte[] array = new byte[toExclusive - from];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(byte[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(byte[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(byte[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, BALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static byte subscript(byte[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] subscript(byte[] array, Range<@Primitive Integer> range)
	{
		final int start = range.first();
		final int count = range.count();
		final byte[] slice = new byte[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, BASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(byte[] array, int index, byte newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(byte[] array, Range<@Primitive Integer> range, byte[] newValues)
	{
		final int start = range.first();
		final int count = range.count();
		System.arraycopy(newValues, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static ByteRef subscript_$amp(byte[] array, int index)
	{
		return new ByteArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(byte[] array, IntConsumer action)
	{
		for (final byte value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(byte[] array, byte value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(byte[] array1, byte[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] $plus(byte[] array, byte value)
	{
		final int len = array.length;
		final byte[] res = new byte[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] $plus$plus(byte[] array1, byte[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final byte[] res = new byte[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] $minus(byte[] array, byte value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final byte[] res = new byte[len - 1];
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
	public static byte[] $minus$minus(byte[] array1, byte[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final byte[] res = new byte[len];

		for (final byte v : array1)
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
	public static byte[] $amp(byte[] array1, byte[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final byte[] res = new byte[len];

		for (final byte v : array1)
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
	public static byte[] mapped(byte[] array, IntUnaryOperator mapper)
	{
		final int len = array.length;
		final byte[] res = new byte[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (byte) mapper.applyAsInt(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] flatMapped(byte[] array, IntFunction<byte[]> mapper)
	{
		int size = 0;
		byte[] res = EMPTY;

		for (final byte v : array)
		{
			final byte[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final byte[] newRes = new byte[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] filtered(byte[] array, IntPredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final byte[] res = new byte[len];
		for (final byte v : array)
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
	public static byte[] sorted(byte[] array)
	{
		final byte[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(byte[] array, byte value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(byte[] array, byte value, int start)
	{
		for (; start < array.length; start++)
		{
			if (array[start] == value)
			{
				return start;
			}
		}
		return -1;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(byte[] array, byte value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(byte[] array, byte value, int start)
	{
		for (; start >= 0; start--)
		{
			if (array[start] == value)
			{
				return start;
			}
		}
		return -1;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean contains(byte[] array, byte value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(byte value, byte[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static byte[] copy(byte[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Byte[] boxed(byte[] array)
	{
		final int len = array.length;
		final Byte[] boxed = new Byte[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Iterable<Byte> toIterable(byte[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(byte[] array1, byte[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(byte[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(byte[] array)
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
	public static void toString(byte[] array, StringBuilder builder)
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
