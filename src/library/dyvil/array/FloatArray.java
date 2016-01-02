package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Float;
import dyvil.lang.Int;
import dyvil.ref.FloatRef;
import dyvil.ref.array.FloatArrayRef;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

import static dyvil.reflect.Opcodes.*;

public interface FloatArray
{
	float[] EMPTY = new float[0];
	
	static float[] apply()
	{
		return EMPTY;
	}
	
	static float[] apply(int count)
	{
		return new float[count];
	}
	
	static float[] repeat(int count, float repeatedValue)
	{
		float[] array = new float[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static float[] generate(int count, DoubleUnaryOperator generator)
	{
		float[] array = new float[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (float) generator.applyAsDouble(i);
		}
		return array;
	}
	
	static float[] apply(float start, float end)
	{
		int i = 0;
		float[] array = new float[(int) (end - start + 1)];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static float[] range(float start, float end)
	{
		int i = 0;
		float[] array = new float[(int) (end - start + 1)];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static float[] rangeOpen(float start, float end)
	{
		int i = 0;
		float[] array = new float[(int) (end - start)];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static int length(float[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, FALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static float subscript(float[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] subscript(float[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		float[] slice = new float[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, FASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(float[] array, int i, float v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static void subscript_$eq(float[] array, Range<Int> range, float[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static FloatRef subscriptRef(float[] array, int index)
	{
		return new FloatArrayRef(array, index);
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	static boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void forEach(int[] array, IntConsumer action)
	{
		for (int v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $qmark(float[] array, float v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $eq$eq(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $bang$eq(float[] array1, float[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] $plus(float[] array, float v)
	{
		int len = array.length;
		float[] res = new float[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] $plus$plus(float[] array1, float[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		float[] res = new float[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] $minus(float[] array, float v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		float[] res = new float[len - 1];
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
	static float[] $minus$minus(float[] array1, float[] array2)
	{
		int index = 0;
		int len = array1.length;
		float[] res = new float[len];
		
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
	static float[] $amp(float[] array1, float[] array2)
	{
		int index = 0;
		int len = array1.length;
		float[] res = new float[len];
		
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
	static float[] mapped(float[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		float[] res = new float[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (float) mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] flatMapped(float[] array, DoubleFunction<float[]> mapper)
	{
		int size = 0;
		float[] res = EMPTY;
		
		for (float v : array)
		{
			float[] a = mapper.apply(v);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				float[] newRes = new float[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] filtered(float[] array, DoublePredicate condition)
	{
		int index = 0;
		int len = array.length;
		float[] res = new float[len];
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
	static float[] sorted(float[] array)
	{
		float[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(float[] array, float v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(float[] array, float v, int start)
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
	static int lastIndexOf(float[] array, float v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(float[] array, float v, int start)
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
	static boolean contains(float[] array, float v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean in(float v, float[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static float[] copy(float[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Float[] boxed(float[] array)
	{
		int len = array.length;
		Float[] boxed = new Float[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Float.apply(array[i]);
		}
		return boxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Iterable<Float> toIterable(float[] array)
	{
		return new ArrayList<Float>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean equals(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int hashCode(float[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String toString(float[] array)
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
	static void toString(float[] array, StringBuilder builder)
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
