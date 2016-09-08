package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.ref.BooleanRef;
import dyvil.ref.array.BooleanArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class BooleanArray
{
	public static final boolean[] EMPTY = new boolean[0];

	private BooleanArray()
	{
		// no instances
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static boolean[] apply(int size)
	{
		return new boolean[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static boolean[] apply()
	{
		return new boolean[0];
	}

	public static boolean[] apply(int size, boolean repeatedValue)
	{
		final boolean[] array = new boolean[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static boolean[] apply(int size, BooleanSupplier valueSupplier)
	{
		final boolean[] array = new boolean[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueSupplier.getAsBoolean();
		}
		return array;
	}

	public static boolean[] apply(int size, IntPredicate valueMapper)
	{
		final boolean[] array = new boolean[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = valueMapper.test(i);
		}
		return array;
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static boolean[] apply(boolean[] array)
	{
		return array.clone();
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(boolean[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(boolean[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(boolean[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, BALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean subscript(boolean[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] subscript(boolean[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final boolean[] result = new boolean[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, BASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(boolean[] array, int index, boolean newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(boolean[] array, Range<@Primitive Integer> range, boolean[] newValue)
	{
		System.arraycopy(newValue, 0, array, range.first(), range.size());
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static BooleanRef subscript_$amp(boolean[] array, int index)
	{
		return new BooleanArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(boolean[] array, Consumer<Boolean> action)
	{
		for (boolean value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(boolean[] array, boolean value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(boolean[] array1, boolean[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(boolean[] array1, boolean[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] $plus(boolean[] array, boolean value)
	{
		final int len = array.length;
		final boolean[] res = new boolean[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] $plus$plus(boolean[] array1, boolean[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final boolean[] res = new boolean[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] $minus(boolean[] array, boolean value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final boolean[] res = new boolean[len - 1];
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
	public static boolean[] $minus$minus(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final boolean[] res = new boolean[len];

		for (boolean value : array1)
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
	public static boolean[] $amp(boolean[] array1, boolean[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final boolean[] res = new boolean[len];

		for (boolean value : array1)
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
	public static boolean[] mapped(boolean[] array, Predicate<Boolean> mapper)
	{
		final int len = array.length;
		final boolean[] res = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.test(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] flatMapped(boolean[] array, Function<Boolean, boolean[]> mapper)
	{
		int size = 0;
		boolean[] res = EMPTY;

		for (boolean value : array)
		{
			final boolean[] a = mapper.apply(value);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final boolean[] newRes = new boolean[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean[] filtered(boolean[] array, Predicate<Boolean> condition)
	{
		int index = 0;
		final int len = array.length;
		final boolean[] res = new boolean[len];
		for (boolean value : array)
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
	public static boolean[] sorted(boolean[] array)
	{
		final int len = array.length;
		if (len <= 0)
		{
			return array;
		}

		final boolean[] res = new boolean[len];

		// Count the number of 'false' in the array
		int falseEntries = 0;

		for (boolean value : array)
		{
			if (!value)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(boolean[] array, boolean value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(boolean[] array, boolean value, int start)
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
	public static int lastIndexOf(boolean[] array, boolean value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(boolean[] array, boolean value, int startIndex)
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
	public static boolean contains(boolean[] array, boolean value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(boolean value, boolean[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean[] copy(boolean[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Boolean[] boxed(boolean[] array)
	{
		final int len = array.length;
		final Boolean[] boxed = new Boolean[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Iterable<Boolean> toIterable(boolean[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(boolean[] array1, boolean[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(boolean[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(boolean[] array)
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

		final StringBuilder buf = new StringBuilder(len * 7 + 2);
		buf.append('[').append(array[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(array[i]);
		}
		return buf.append(']').toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void toString(boolean[] array, StringBuilder builder)
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
