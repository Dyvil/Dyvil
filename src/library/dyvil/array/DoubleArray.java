package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;

public interface DoubleArray
{
	public static final double[]	EMPTY	= new double[0];
	
	public static double[] apply()
	{
		return EMPTY;
	}
	
	public static double[] apply(int count)
	{
		return new double[count];
	}
	
	public static double[] apply(int count, double repeatedValue)
	{
		double[] array = new double[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static double[] apply(int count, DoubleUnaryOperator generator)
	{
		double[] array = new double[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.applyAsDouble(i);
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix int length(double[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DALOAD })
	public static @infix double apply(double[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DASTORE })
	public static @infix void update(double[] array, int i, double v)
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
	
	public static @infix @inline boolean $qmark(double[] array, double v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	public static @infix @inline boolean $eq$eq(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline boolean $bang$eq(double[] array1, double[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	public static @infix double[] $plus(double[] array, double v)
	{
		int len = array.length;
		double[] res = new double[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix double[] $plus$plus(double[] array1, double[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		double[] res = new double[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix double[] $minus(double[] array, double v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		double[] res = new double[len - 1];
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
	
	public static @infix double[] $minus$minus(double[] array1, double[] array2)
	{
		int index = 0;
		int len = array1.length;
		double[] res = new double[len];
		
		for (int i = 0; i < len; i++)
		{
			double v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix double[] $amp(double[] array1, double[] array2)
	{
		int index = 0;
		int len = array1.length;
		double[] res = new double[len];
		
		for (int i = 0; i < len; i++)
		{
			double v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix double[] mapped(double[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		double[] res = new double[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	public static @infix double[] filtered(double[] array, DoublePredicate condition)
	{
		int index = 0;
		int len = array.length;
		double[] res = new double[len];
		for (int i = 0; i < len; i++)
		{
			double v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix double[] sorted(double[] array)
	{
		double[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	public static @infix int indexOf(double[] array, double v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix int indexOf(double[] array, double v, int start)
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
	
	public static @infix int lastIndexOf(double[] array, double v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix int lastIndexOf(double[] array, double v, int start)
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
	
	public static @infix @inline boolean contains(double[] array, double v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	public static @infix @inline boolean in(double v, double[] array)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	// Copying
	
	public static @infix @inline double[] copy(double[] array)
	{
		return array.clone();
	}
	
	// equals, hashCode and toString
	
	public static @infix @inline boolean equals(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline int hashCode(double[] array)
	{
		return Arrays.hashCode(array);
	}
	
	public static @infix String toString(double[] array)
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
	
	public static @infix void toString(double[] array, StringBuilder builder)
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
