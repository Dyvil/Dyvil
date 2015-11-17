package dyvil.array;

import java.util.Arrays;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

import dyvil.lang.Float;
import dyvil.lang.Int;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;

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
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH })
	static @infix int length(float[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, FALOAD })
	static @infix float subscript(float[] array, int i)
	{
		return array[i];
	}
	
	static @infix float[] subscript(float[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = Int.unapply(range.last()) - start + 1;
		float[] slice = new float[count];
		for (int i = 0; i < count; i++)
		{
			slice[i] = array[start + i];
		}
		return slice;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, FASTORE })
	static @infix void subscript_$eq(float[] array, int i, float v)
	{
		array[i] = v;
	}
	
	static @infix void subscript_$eq(float[] array, Range<Int> range, float[] values)
	{
		int start = Int.unapply(range.first());
		int count = Int.unapply(range.last()) - start + 1;
		for (int i = 0; i < count; i++)
		{
			array[start + i] = values[i];
		}
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH, IFEQ })
	static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	static @infix void forEach(int[] array, IntConsumer action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	static @infix @inline boolean $qmark(float[] array, float v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	static @infix @inline boolean $eq$eq(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static @infix @inline boolean $bang$eq(float[] array1, float[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	static @infix float[] $plus(float[] array, float v)
	{
		int len = array.length;
		float[] res = new float[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	static @infix float[] $plus$plus(float[] array1, float[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		float[] res = new float[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	static @infix float[] $minus(float[] array, float v)
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
	
	static @infix float[] $minus$minus(float[] array1, float[] array2)
	{
		int index = 0;
		int len = array1.length;
		float[] res = new float[len];
		
		for (int i = 0; i < len; i++)
		{
			float v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix float[] $amp(float[] array1, float[] array2)
	{
		int index = 0;
		int len = array1.length;
		float[] res = new float[len];
		
		for (int i = 0; i < len; i++)
		{
			float v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix float[] mapped(float[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		float[] res = new float[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (float) mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	static @infix float[] flatMapped(float[] array, DoubleFunction<float[]> mapper)
	{
		int len = array.length;
		int size = 0;
		float[] res = EMPTY;
		
		for (int i = 0; i < len; i++)
		{
			float[] a = mapper.apply(array[i]);
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
	
	static @infix float[] filtered(float[] array, DoublePredicate condition)
	{
		int index = 0;
		int len = array.length;
		float[] res = new float[len];
		for (int i = 0; i < len; i++)
		{
			float v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	static @infix float[] sorted(float[] array)
	{
		float[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	static @infix int indexOf(float[] array, float v)
	{
		return indexOf(array, v, 0);
	}
	
	static @infix int indexOf(float[] array, float v, int start)
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
	
	static @infix int lastIndexOf(float[] array, float v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	static @infix int lastIndexOf(float[] array, float v, int start)
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
	
	static @infix @inline boolean contains(float[] array, float v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	static @infix @inline boolean in(float v, float[] array)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	// Copying
	
	static @infix @inline float[] copy(float[] array)
	{
		return array.clone();
	}
	
	static @infix Float[] boxed(float[] array)
	{
		int len = array.length;
		Float[] boxed = new Float[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Float.apply(array[i]);
		}
		return boxed;
	}
	
	static @infix Iterable<Float> toIterable(float[] array)
	{
		return new ArrayList<Float>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	static @infix @inline boolean equals(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static @infix @inline int hashCode(float[] array)
	{
		return Arrays.hashCode(array);
	}
	
	static @infix String toString(float[] array)
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
	
	static @infix void toString(float[] array, StringBuilder builder)
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
