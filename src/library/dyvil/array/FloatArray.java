package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;

public interface FloatArray
{
	public static final float[]	EMPTY	= new float[0];
	
	public static float[] apply()
	{
		return EMPTY;
	}
	
	public static float[] apply(int count)
	{
		return new float[count];
	}
	
	public static float[] apply(int count, int repeatedValue)
	{
		float[] array = new float[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static float[] apply(int count, DoubleUnaryOperator generator)
	{
		float[] array = new float[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = (float) generator.applyAsDouble(i);
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(float[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FALOAD })
	public static @infix float apply(float[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FASTORE })
	public static @infix void update(float[] array, int i, float v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	public static @infix void forEach(int[] array, IntConsumer action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix float[] $plus(float[] array, float v)
	{
		int len = array.length;
		float[] res = new float[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix float[] $plus$plus(float[] array1, float[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		float[] res = new float[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix float[] $minus(float[] array, float v)
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
	
	public static @infix float[] $minus$minus(float[] array1, float[] array2)
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
	
	public static @infix float[] $amp(float[] array1, float[] array2)
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
	
	public static @infix float[] mapped(float[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		float[] res = new float[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = (float) mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	public static @infix float[] filtered(float[] array, DoublePredicate condition)
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
	
	public static @infix float[] sorted(float[] array)
	{
		float[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	public static @infix @inline boolean $eq$eq(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline boolean $bang$eq(float[] array1, float[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	public static @infix @inline boolean equals(float[] array1, float[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline int hashCode(float[] array)
	{
		return Arrays.hashCode(array);
	}
	
	public static @infix String toString(float[] array)
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
	
	public static @infix void toString(float[] array, StringBuilder builder)
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
	
	// Search Operations
	
	public static @infix int indexOf(float[] array, float v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(float[] array, float v, int start)
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
	
	public static @infix int lastIndexOf(float[] array, float v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(float[] array, float v, int start)
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
	
	public static @infix boolean contains(float[] array, float v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
