package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Double;
import dyvil.lang.Int;
import dyvil.reflect.Modifiers;

import java.util.Arrays;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

import static dyvil.reflect.Opcodes.*;

public interface DoubleArray
{
	double[] EMPTY = new double[0];
	
	static double[] apply()
	{
		return EMPTY;
	}
	
	static double[] apply(int count)
	{
		return new double[count];
	}
	
	static double[] repeat(int count, double repeatedValue)
	{
		double[] array = new double[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static double[] generate(int count, DoubleUnaryOperator generator)
	{
		double[] array = new double[count];
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.applyAsDouble(i);
		}
		return array;
	}
	
	static double[] range(double start, double end)
	{
		int i = 0;
		double[] array = new double[(int) (end - start + 1)];
		for (; start <= end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	static double[] rangeOpen(double start, double end)
	{
		int i = 0;
		double[] array = new double[(int) (end - start)];
		for (; start < end; start++)
		{
			array[i++] = start;
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static int length(double[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, DALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static double subscript(double[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] subscript(double[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		double[] slice = new double[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, DASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	static void subscript_$eq(double[] array, int i, double v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static void subscript_$eq(double[] array, Range<Int> range, double[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
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
	static boolean $qmark(double[] array, double v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $eq$eq(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $bang$eq(double[] array1, double[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] $plus(double[] array, double v)
	{
		int len = array.length;
		double[] res = new double[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] $plus$plus(double[] array1, double[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		double[] res = new double[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] $minus(double[] array, double v)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] $minus$minus(double[] array1, double[] array2)
	{
		int index = 0;
		int len = array1.length;
		double[] res = new double[len];
		
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
	static double[] $amp(double[] array1, double[] array2)
	{
		int index = 0;
		int len = array1.length;
		double[] res = new double[len];
		
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
	static double[] mapped(double[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		double[] res = new double[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] flatMapped(double[] array, DoubleFunction<double[]> mapper)
	{
		int size = 0;
		double[] res = EMPTY;
		
		for (double v : array)
		{
			double[] a = mapper.apply(v);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				double[] newRes = new double[size + alen];
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
		}
		
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] filtered(double[] array, DoublePredicate condition)
	{
		int index = 0;
		int len = array.length;
		double[] res = new double[len];
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
	static double[] sorted(double[] array)
	{
		double[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(double[] array, double v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int indexOf(double[] array, double v, int start)
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
	static int lastIndexOf(double[] array, double v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int lastIndexOf(double[] array, double v, int start)
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
	static boolean contains(double[] array, double v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean in(double v, double[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static double[] copy(double[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Double[] boxed(double[] array)
	{
		int len = array.length;
		Double[] boxed = new Double[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Double.apply(array[i]);
		}
		return boxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static Iterable<Double> toIterable(double[] array)
	{
		return new ArrayList<Double>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean equals(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static int hashCode(double[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static String toString(double[] array)
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
	static void toString(double[] array, StringBuilder builder)
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
