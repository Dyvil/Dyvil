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
import dyvil.collection.range.closed.DoubleRange;
import dyvil.ref.DoubleRef;
import dyvil.ref.array.DoubleArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

@SuppressWarnings({ "cast", "RedundantCast" })
public abstract class DoubleArray
{
	public static final double[] EMPTY = new double[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static double[] apply()
	{
		return new double[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static double[] apply(int size)
	{
		return new double[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static double[] apply(double[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.IMPLICIT | Modifiers.INLINE)
	public static double[] apply(DoubleRange range)
	{
		return range.toDoubleArray();
	}

	public static double[] apply(int size, double repeatedValue)
	{
		final double[] array = new double[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static double[] apply(int size, DoubleSupplier valueSupplier)
	{
		final double[] array = new double[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (double) valueSupplier.getAsDouble();
		}
		return array;
	}

	public static double[] apply(int size, DoubleUnaryOperator valueMapper)
	{
		final double[] array = new double[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (double) valueMapper.applyAsDouble(i);
		}
		return array;
	}

	@DyvilName("apply")
	public static double[] rangeClosed(double from, double to)
	{
		int index = 0;
		final double[] array = new double[(int) (to - from + 1)];
		for (; from <= to; from++)
		{
			array[index++] = from;
		}
		return array;
	}

	@DyvilName("apply")
	public static double[] range(double from, double toExclusive)
	{
		int index = 0;
		final double[] array = new double[(int) (toExclusive - from)];
		for (; from < toExclusive; from++)
		{
			array[index++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(double[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(double[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(double[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, DALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static double subscript(double[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] subscript(double[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final double[] result = new double[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, DASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(double[] array, int index, double newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(double[] array, Range<@Primitive Integer> range, double[] newValues)
	{
		System.arraycopy(newValues, 0, array, range.first(), range.size());
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static DoubleRef subscript_$amp(double[] array, int index)
	{
		return new DoubleArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(double[] array, DoubleConsumer action)
	{
		for (double value : array)
		{
			action.accept(value);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(double[] array, double value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(double[] array1, double[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] $plus(double[] array, double value)
	{
		final int size = array.length;
		final double[] res = new double[size + 1];
		System.arraycopy(array, 0, res, 0, size);
		res[size] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] $plus$plus(double[] array1, double[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final double[] res = new double[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] $minus(double[] array, double value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int size = array.length;
		final double[] res = new double[size - 1];
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
	public static double[] $minus$minus(double[] array1, double[] array2)
	{
		int index = 0;
		final int size = array1.length;
		final double[] res = new double[size];

		for (double v : array1)
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
	public static double[] $amp(double[] array1, double[] array2)
	{
		int index = 0;
		final int size = array1.length;
		final double[] res = new double[size];

		for (double v : array1)
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
	public static double[] mapped(double[] array, DoubleUnaryOperator mapper)
	{
		final int size = array.length;
		final double[] res = new double[size];
		for (int i = 0; i < size; i++)
		{
			res[i] = (double) mapper.applyAsDouble(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] flatMapped(double[] array, DoubleFunction<double[]> mapper)
	{
		int size = 0;
		double[] res = EMPTY;

		for (double v : array)
		{
			final double[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final double[] newRes = new double[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double[] filtered(double[] array, DoublePredicate condition)
	{
		int index = 0;
		final int size = array.length;
		final double[] res = new double[size];
		for (double v : array)
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
	public static double[] sorted(double[] array)
	{
		final double[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(double[] array, double value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(double[] array, double value, int startIndex)
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
	public static int lastIndexOf(double[] array, double value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(double[] array, double value, int startIndex)
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
	public static boolean contains(double[] array, double value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(double value, double[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static double[] copy(double[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Double[] boxed(double[] array)
	{
		final int size = array.length;
		final Double[] boxed = new Double[size];
		for (int i = 0; i < size; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static Iterable<@Primitive Double> asIterable(double[] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.IMPLICIT)
	public static ImmutableList<@Primitive Double> asList(double @Immutable [] array)
	{
		return toList(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static ImmutableList<@Primitive Double> toList(double[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(double[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(double[] array)
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
	public static void toString(double[] array, StringBuilder builder)
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
