package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.lang.Double;
import dyvil.lang.Int;

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
	
	@Intrinsic( { LOAD_0, LOAD_1, ARRAYLENGTH })
	static
	@infix
	int length(double[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, DALOAD })
	static
	@infix
	double subscript(double[] array, int i)
	{
		return array[i];
	}
	
	static
	@infix
	double[] subscript(double[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		double[] slice = new double[count];
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, DASTORE })
	static
	@infix
	void subscript_$eq(double[] array, int i, double v)
	{
		array[i] = v;
	}
	
	static
	@infix
	void subscript_$eq(double[] array, Range<Int> range, double[] values)
	{
		int start = Int.unapply(range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, ARRAYLENGTH, IFEQ })
	static
	@infix
	boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	static
	@infix
	void forEach(int[] array, IntConsumer action)
	{
		for (int v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	static
	@infix
	@inline
	boolean $qmark(double[] array, double v)
	{
		return Arrays.binarySearch(array, v) >= 0;
	}
	
	static
	@infix
	@inline
	boolean $eq$eq(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static
	@infix
	@inline
	boolean $bang$eq(double[] array1, double[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	static
	@infix
	double[] $plus(double[] array, double v)
	{
		int len = array.length;
		double[] res = new double[len + 1];
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	static
	@infix
	double[] $plus$plus(double[] array1, double[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		double[] res = new double[len1 + len2];
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	static
	@infix
	double[] $minus(double[] array, double v)
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
	
	static
	@infix
	double[] $minus$minus(double[] array1, double[] array2)
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
	
	static
	@infix
	double[] $amp(double[] array1, double[] array2)
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
	
	static
	@infix
	double[] mapped(double[] array, DoubleUnaryOperator mapper)
	{
		int len = array.length;
		double[] res = new double[len];
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.applyAsDouble(array[i]);
		}
		return res;
	}
	
	static
	@infix
	double[] flatMapped(double[] array, DoubleFunction<double[]> mapper)
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
	
	static
	@infix
	double[] filtered(double[] array, DoublePredicate condition)
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
	
	static
	@infix
	double[] sorted(double[] array)
	{
		double[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	// Search Operations
	
	static
	@infix
	int indexOf(double[] array, double v)
	{
		return indexOf(array, v, 0);
	}
	
	static
	@infix
	int indexOf(double[] array, double v, int start)
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
	
	static
	@infix
	int lastIndexOf(double[] array, double v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	static
	@infix
	int lastIndexOf(double[] array, double v, int start)
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
	
	static
	@infix
	@inline
	boolean contains(double[] array, double v)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	static
	@infix
	@inline
	boolean in(double v, double[] array)
	{
		return indexOf(array, v, 0) >= 0;
	}
	
	// Copying
	
	static
	@infix
	@inline
	double[] copy(double[] array)
	{
		return array.clone();
	}
	
	static
	@infix
	Double[] boxed(double[] array)
	{
		int len = array.length;
		Double[] boxed = new Double[len];
		for (int i = 0; i < len; i++)
		{
			boxed[i] = Double.apply(array[i]);
		}
		return boxed;
	}
	
	static
	@infix
	Iterable<Double> toIterable(double[] array)
	{
		return new ArrayList<Double>(boxed(array), true);
	}
	
	// equals, hashCode and toString
	
	static
	@infix
	@inline
	boolean equals(double[] array1, double[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	static
	@infix
	@inline
	int hashCode(double[] array)
	{
		return Arrays.hashCode(array);
	}
	
	static
	@infix
	String toString(double[] array)
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
	
	static
	@infix
	void toString(double[] array, StringBuilder builder)
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
