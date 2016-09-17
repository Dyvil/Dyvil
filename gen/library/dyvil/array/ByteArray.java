// GENERATED SOURCE - DOT NOT EDIT

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
import dyvil.ref.ByteRef;
import dyvil.ref.array.ByteArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

@SuppressWarnings({ "cast", "RedundantCast" })
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

	public static byte[] apply(int size, IntSupplier valueSupplier)
	{
		final byte[] array = new byte[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (byte) valueSupplier.getAsInt();
		}
		return array;
	}

	public static byte[] apply(int size, IntUnaryOperator valueMapper)
	{
		final byte[] array = new byte[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (byte) valueMapper.applyAsInt(i);
		}
		return array;
	}

	@DyvilName("apply")
	public static byte[] rangeClosed(byte from, byte to)
	{
		int index = 0;
		final byte[] array = new byte[(int) (to - from + 1)];
		for (; from <= to; from++)
		{
			array[index++] = from;
		}
		return array;
	}

	@DyvilName("apply")
	public static byte[] range(byte from, byte toExclusive)
	{
		int index = 0;
		final byte[] array = new byte[(int) (toExclusive - from)];
		for (; from < toExclusive; from++)
		{
			array[index++] = from;
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
		final int size = range.size();
		final byte[] result = new byte[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
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
		System.arraycopy(newValues, 0, array, range.first(), range.size());
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
		for (byte value : array)
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
		final int size = array.length;
		final byte[] res = new byte[size + 1];
		System.arraycopy(array, 0, res, 0, size);
		res[size] = value;
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

		final int size = array.length;
		final byte[] res = new byte[size - 1];
		if (index > 0)
		{
			// copy the first part before the index
			System.arraycopy(array, 0, res, 0, index);
		}
		if (index < size)
		{
			// copy the second part after the index
			System.arraycopy(array, index + 1, res, index, size - index - 1);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte[] $minus$minus(byte[] array1, byte[] array2)
	{
		int index = 0;
		final int size = array1.length;
		final byte[] res = new byte[size];

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
	public static byte[] $amp(byte[] array1, byte[] array2)
	{
		int index = 0;
		final int size = array1.length;
		final byte[] res = new byte[size];

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
	public static byte[] mapped(byte[] array, IntUnaryOperator mapper)
	{
		final int size = array.length;
		final byte[] res = new byte[size];
		for (int i = 0; i < size; i++)
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

		for (byte v : array)
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
		final int size = array.length;
		final byte[] res = new byte[size];
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
	public static int indexOf(byte[] array, byte value, int startIndex)
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
	public static int lastIndexOf(byte[] array, byte value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(byte[] array, byte value, int startIndex)
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
		final int size = array.length;
		final Byte[] boxed = new Byte[size];
		for (int i = 0; i < size; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static Iterable<@Primitive Byte> asIterable(byte[] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static ImmutableList<@Primitive Byte> asList(byte @Immutable [] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static ImmutableList<@Primitive Byte> toList(byte[] array)
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

		final int size = array.length;
		if (size <= 0)
		{
			return "[]";
		}

		final StringBuilder buf = new StringBuilder(size * 3 + 4);
		buf.append('[').append(array[0]);
		for (int i = 1; i < size; i++)
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

		final int size = array.length;
		if (size <= 0)
		{
			builder.append("[]");
			return;
		}

		builder.append('[').append(array[0]);
		for (int i = 1; i < size; i++)
		{
			builder.append(", ");
			builder.append(array[i]);
		}
		builder.append(']');
	}
}
