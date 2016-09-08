package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.annotation._internal.Primitive;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.range.closed.FloatRange;
import dyvil.ref.FloatRef;
import dyvil.ref.array.FloatArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.*;

import static dyvil.reflect.Opcodes.*;

public abstract class FloatArray
{
	public static final float[] EMPTY = new float[0];

	@DyvilModifiers(Modifiers.INLINE)
	public static float[] apply()
	{
		return new float[0];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static float[] apply(int size)
	{
		return new float[size];
	}

	@DyvilModifiers(Modifiers.INLINE)
	public static float[] apply(float[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.IMPLICIT | Modifiers.INLINE)
	public static float[] apply(FloatRange range)
	{
		return range.toFloatArray();
	}

	public static float[] apply(int size, float repeatedValue)
	{
		final float[] array = new float[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}

	public static float[] apply(int size, DoubleSupplier valueSupplier)
	{
		final float[] array = new float[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (float) valueSupplier.getAsDouble();
		}
		return array;
	}

	public static float[] apply(int size, DoubleUnaryOperator valueMapper)
	{
		final float[] array = new float[size];
		for (int i = 0; i < size; i++)
		{
			array[i] = (float) valueMapper.applyAsDouble(i);
		}
		return array;
	}

	public static float[] apply_$_closed(float from, float to)
	{
		int i = 0;
		final float[] array = new float[(int) (to - from + 1)];
		for (; from <= to; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	public static float[] apply_$_halfOpen(float from, float toExclusive)
	{
		int i = 0;
		final float[] array = new float[(int) (toExclusive - from)];
		for (; from < toExclusive; from++)
		{
			array[i++] = from;
		}
		return array;
	}

	// Basic Array Operations

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int length(float[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	public static int size(float[] array)
	{
		return array.length;
	}

	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isEmpty(float[] array)
	{
		return array.length == 0;
	}

	@Intrinsic( { LOAD_0, LOAD_1, FALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	public static float subscript(float[] array, int index)
	{
		return array[index];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] subscript(float[] array, Range<@Primitive Integer> range)
	{
		final int size = range.size();
		final float[] result = new float[size];
		System.arraycopy(array, range.first(), result, 0, size);
		return result;
	}

	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, FASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(float[] array, int index, float newValue)
	{
		array[index] = newValue;
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static void subscript_$eq(float[] array, Range<@Primitive Integer> range, float[] newValues)
	{
		System.arraycopy(newValues, 0, array, range.first(), range.size());
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	public static FloatRef subscript_$amp(float[] array, int index)
	{
		return new FloatArrayRef(array, index);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void forEach(float[] array, DoubleConsumer action)
	{
		for (float v : array)
		{
			action.accept(v);
		}
	}

	// Operators

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $qmark(float[] array, float value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $eq$eq(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean $bang$eq(float[] array1, float[] array2)
	{
		return !Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] $plus(float[] array, float value)
	{
		final int len = array.length;
		final float[] res = new float[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = value;
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] $plus$plus(float[] array1, float[] array2)
	{
		final int len1 = array1.length;
		final int len2 = array2.length;
		final float[] res = new float[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] $minus(float[] array, float value)
	{
		final int index = indexOf(array, value, 0);
		if (index < 0)
		{
			return array;
		}

		final int len = array.length;
		final float[] res = new float[len - 1];
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
	public static float[] $minus$minus(float[] array1, float[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final float[] res = new float[len];

		for (float v : array1)
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
	public static float[] $amp(float[] array1, float[] array2)
	{
		int index = 0;
		final int len = array1.length;
		final float[] res = new float[len];

		for (float v : array1)
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
	public static float[] mapped(float[] array, DoubleUnaryOperator mapper)
	{
		final int len = array.length;
		final float[] res = new float[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (float) mapper.applyAsDouble(array[i]);
		}
		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] flatMapped(float[] array, DoubleFunction<float[]> mapper)
	{
		int size = 0;
		float[] res = EMPTY;

		for (float v : array)
		{
			final float[] a = mapper.apply(v);
			final int alen = a.length;
			if (size + alen >= res.length)
			{
				final float[] newRes = new float[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}

			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}

		return res;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float[] filtered(float[] array, DoublePredicate condition)
	{
		int index = 0;
		final int len = array.length;
		final float[] res = new float[len];
		for (float v : array)
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
	public static float[] sorted(float[] array)
	{
		final float[] res = array.clone();
		Arrays.sort(res);
		return res;
	}

	// Search Operations

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(float[] array, float value)
	{
		return indexOf(array, value, 0);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOf(float[] array, float value, int startIndex)
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
	public static int lastIndexOf(float[] array, float value)
	{
		return lastIndexOf(array, value, array.length - 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int lastIndexOf(float[] array, float value, int startIndex)
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
	public static boolean contains(float[] array, float value)
	{
		return indexOf(array, value, 0) >= 0;
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean in(float value, float[] array)
	{
		return indexOf(array, value, 0) >= 0;
	}

	// Copying

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static float[] copy(float[] array)
	{
		return array.clone();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Float[] boxed(float[] array)
	{
		final int len = array.length;
		final Float[] boxed = new Float[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = array[i];
		}
		return boxed;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static Iterable<Float> toIterable(float[] array)
	{
		return new ArrayList<>(boxed(array), true);
	}

	// equals, hashCode and toString

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static boolean equals(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static int hashCode(float[] array)
	{
		return Arrays.hashCode(array);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(float[] array)
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
	public static void toString(float[] array, StringBuilder builder)
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
