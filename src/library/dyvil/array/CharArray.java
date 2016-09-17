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
import dyvil.ref.CharRef;
import dyvil.ref.array.CharArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class CharArray
{
	public static final char[] EMPTY = new char[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static char[] apply()
	{
		return new char[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static char[] apply(int size)
	{
		return new char[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static char[] apply(char[] array)
	{
		return array.clone();
	}

	public static char[] apply(int size, char repeatedValue)
	{
		final char[] array = new char[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static char[] apply(int size, IntSupplier valueSupplier)
	{
		final char[] array = new char[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (char) valueSupplier.getAsInt();
		}
		return array;
	}

	public static char[] apply(int size, IntUnaryOperator valueMapper)
	{
		final char[] array = new char[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (char) valueMapper.applyAsInt(i);
		}
		return array;
	}

	@DyvilName("apply")
	public static char[] rangeClosed(char from, char to)
	{
		int i = 0;
		final char[] array = new char[to - from + 1];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	@DyvilName("apply")
	public static char[] range(char from, char toExclusive)
	{
		int i = 0;
		final char[] array = new char[toExclusive - from];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(char[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(char[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(char[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, CALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static char subscript(char[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] subscript(char[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final char[] result = new char[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, CASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(char[] array, int index, char value)
	{
		array[index] = value;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(char[] array, Range<@Primitive Integer> range, char[] newValues)
	{
		System.arraycopy(newValues, 0, array, range.first(), range.size());
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static CharRef subscript_$amp(char[] array, int index)
	{
		return new CharArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(char[] array, IntConsumer action)
	{
		for (char value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(char[] array, char value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(char[] array1, char[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(char[] array1, char[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] $plus(char[] array, char value)
	{
		final int len = array.length;
		final char[] res = new char[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] $plus$plus(char[] array1, char[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final char[] res = new char[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] $minus(char[] array, char value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final char[] res = new char[len - 1];
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
	public static char[] $minus$minus(char[] array1, char[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final char[] res = new char[len];

		for (char value : array1)
		{
			if (indexOf(array2, value, 0) < 0)
			{
				res[index++] = value;
			}
		}

		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] $amp(char[] array1, char[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final char[] res = new char[len];

		for (char value : array1)
		{
			if (indexOf(array2, value, 0) >= 0)
			{
				res[index++] = value;
			}
		}

		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] mapped(char[] array, IntUnaryOperator mapper)
	{
		final int len = array.length;
		final char[] res = new char[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (char) mapper.applyAsInt(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] flatMapped(char[] array, IntFunction<char[]> mapper)
	{
		int size = 0;
		char[] res = EMPTY;

		for (char value : array)
		{
			final char[] a = mapper.apply(value);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final char[] newRes = new char[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] filtered(char[] array, IntPredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final char[] res = new char[len];
		for (char value : array)
		{
			if (condition.test(value))
			{
				res[index++] = value;
			}
		}

		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char[] sorted(char[] array)
	{
		final char[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(char[] array, char value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(char[] array, char value, int startIndex)
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
	public static int lastIndexOf(char[] array, char value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(char[] array, char value, int startIndex)
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
	public static boolean contains(char[] array, char value)
	{
		return indexOf(array, value, 0) != -1;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(char value, char[] array)
	{
		return indexOf(array, value, 0) != -1;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static char[] copy(char[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Character[] boxed(char[] array)
	{
		final int len = array.length;
		final Character[] boxed = new Character[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static Iterable<@Primitive Character> asIterable(char[] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static ImmutableList<@Primitive Character> asList(char @Immutable [] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static ImmutableList<@Primitive Character> toList(char[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(char[] array1, char[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(char[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String asString(char[] array)
	{
		return new String(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(char[] array)
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
	public static void toString(char[] array, StringBuilder builder)
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
