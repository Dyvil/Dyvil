package dyvil.arrays;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface DoubleArray
{
	public static final double[]	EMPTY	= new double[0];
	
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
	
	// Operators
	
	public static @infix double[] $plus(double[] a, double[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		double[] res = new double[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
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
	
	public static @infix boolean contains(double[] array, double v)
	{
		return indexOf(array, v, 0) != -1;
	}
}
