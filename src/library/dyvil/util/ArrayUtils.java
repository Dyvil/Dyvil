package dyvil.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dyvil.lang.annotation.implicit;
import dyvil.lang.array.Arrays;

/**
 * The ArrayUtils class provides more advanced array operations such as getting
 * the component type of an array, splitting or concatenating arrays, or
 * removing duplicate objects from an array.
 * 
 * @see Arrays
 * @author Clashsoft
 */
public class ArrayUtils
{
	public static @implicit <T> Class<T> getComponentType(T[] array)
	{
		return (Class<T>) array.getClass().getComponentType();
	}
	
	public static @implicit <T> Class getDeepComponentType(T[] array)
	{
		Class ret = array.getClass();
		while (true)
		{
			Class c = ret.getComponentType();
			if (c == null)
			{
				return ret;
			}
			ret = c;
		}
	}
	
	public static @implicit <T> T[][] split(T[] array, int maxLength)
	{
		Class clazz = array.getClass().getComponentType();
		int arrays = MathUtils.ceil(array.length / maxLength);
		T[][] ret = (T[][]) Array.newInstance(clazz, arrays, maxLength);
		
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = (T[]) Array.newInstance(clazz, maxLength);
			int len1 = i * maxLength;
			for (int j = 0; j < maxLength && j + len1 < array.length; j++)
			{
				ret[i][j] = array[j + len1];
			}
		}
		return ret;
	}
	
	public static <T> T[] concat(T[]... arrays)
	{
		List<T> list = new ArrayList(arrays.length);
		for (T[] array : arrays)
		{
			for (T element : array)
			{
				list.add(element);
			}
		}
		return Arrays.toArray(list, (Class<T>) arrays.getClass().getComponentType());
	}
	
	public static @implicit <T> T[] removeDuplicates(T... array)
	{
		if (array != null && array.length > 0)
		{
			List<T> result = new ArrayList<T>(array.length);
			outer:
			for (T t1 : array)
			{
				for (T t2 : result)
				{
					if (Objects.equals(t1, t2))
					{
						continue outer;
					}
				}
				result.add(t1);
			}
			return Arrays.toArray(result, getComponentType(array));
		}
		return array;
	}
	
	public static Object[] primitiveTransform(Object array)
	{
		Object[] result;
		Class c = array.getClass();
		if (c == boolean[].class)
		{
			boolean[] array1 = (boolean[]) array;
			result = new Boolean[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Boolean.valueOf(array1[i]);
			}
			return result;
		}
		if (c == byte[].class)
		{
			byte[] array1 = (byte[]) array;
			result = new Byte[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Byte.valueOf(array1[i]);
			}
			return result;
		}
		if (c == short[].class)
		{
			short[] array1 = (short[]) array;
			result = new Short[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Short.valueOf(array1[i]);
			}
			return result;
		}
		if (c == int[].class)
		{
			int[] array1 = (int[]) array;
			result = new Integer[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Integer.valueOf(array1[i]);
			}
			return result;
		}
		if (c == long[].class)
		{
			long[] array1 = (long[]) array;
			result = new Long[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Long.valueOf(array1[i]);
			}
			return result;
		}
		if (c == float[].class)
		{
			float[] array1 = (float[]) array;
			result = new Float[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Float.valueOf(array1[i]);
			}
			return result;
		}
		if (c == double[].class)
		{
			double[] array1 = (double[]) array;
			result = new Double[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Double.valueOf(array1[i]);
			}
			return result;
		}
		return null;
	}
}
