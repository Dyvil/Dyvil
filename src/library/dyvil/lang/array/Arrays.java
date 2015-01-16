package dyvil.lang.array;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import dyvil.collections.Collections;

/**
 * The class CSArrays.
 * <p>
 * This class adds several array utils.
 * 
 * @author Clashsoft
 */
public class Arrays
{
	/**
	 * Creates a new array (actually just returns the given argument, but its a
	 * varargs argument)
	 * 
	 * @param array
	 *            the varargs array
	 * @return the array
	 */
	public static <T> T[] create(T... array)
	{
		return array;
	}
	
	/**
	 * Creates a new empty array of type {@code type} and size {@code size}
	 * 
	 * @param size
	 *            the expected size
	 * @param type
	 *            the expected type
	 * @return the array
	 */
	public static <T> T[] create(int size, Class<T> type)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	/**
	 * Creates a new array of size {@code size} and fills it with {@code array}.
	 * <p>
	 * Uses the class of the given argument
	 * 
	 * @see Arrays#create(int, Class)
	 * @see System#arraycopy(Object, int, Object, int, int)
	 * @param size
	 *            the expected size
	 * @param array
	 *            the source array
	 * @return
	 */
	public static <T> T[] create(int size, T... array)
	{
		T[] array1 = (T[]) create(array.getClass().getComponentType(), size);
		System.arraycopy(array, 0, array1, 0, array.length);
		return array1;
	}
	
	/**
	 * Returns the component type of a one-dimensional array
	 * 
	 * @see Class#getComponentType()
	 * @param array
	 *            the array
	 * @return the component type
	 */
	public static <T> Class<T> getComponentType(T[] array)
	{
		return (Class<T>) array.getClass().getComponentType();
	}
	
	/**
	 * Returns the component type of a multi-dimensional array
	 * 
	 * @param array
	 *            the array
	 * @return the component type
	 */
	public static <T> Class getDeepComponentType(T[] array)
	{
		Class ret = getComponentType(array);
		while (true)
		{
			Class c = ret.getComponentType();
			if (c != null && c != ret)
			{
				ret = c;
			}
			else
			{
				break;
			}
		}
		return ret;
	}
	
	public static <T> T[] sort(T[] array)
	{
		java.util.Arrays.sort(array);
		return array;
	}
	
	public static <T> T[] copyOf(T[] array, int newLength)
	{
		return java.util.Arrays.copyOf(array, newLength);
	}
	
	/**
	 * Splits an array into sub-arrays of size {@code maxLength}
	 * 
	 * @param array
	 *            the array
	 * @param maxLength
	 *            the maximum length
	 * @return the two-dimensional array
	 */
	public static <T> T[][] split(T[] array, int maxLength)
	{
		Class clazz = array.getClass().getComponentType();
		int arrays = array.length / maxLength + 1;
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
	
	/**
	 * Concats all arrays
	 * 
	 * @param arrays
	 *            the arrays to concat
	 * @return the array
	 */
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
		return (T[]) fromList(getDeepComponentType(arrays), list);
	}
	
	/**
	 * Removes all duplicates from an array
	 * 
	 * @param array
	 *            the array
	 * @return the array without duplicates
	 */
	public static <T> T[] removeDuplicates(T... array)
	{
		if (array != null && array.length > 0)
		{
			List<T> result = new ArrayList<T>(array.length);
			for (T t1 : array)
			{
				boolean duplicate = false;
				for (T t2 : result)
				{
					if (Objects.equals(t1, t2))
					{
						duplicate = true;
					}
					break;
				}
				if (!duplicate)
				{
					result.add(t1);
				}
			}
			return fromList(getComponentType(array), result);
		}
		return array;
	}
	
	/**
	 * Creates a new Array List from the array
	 * 
	 * @see ArrayList
	 * @param array
	 *            the array
	 * @return an ArrayList
	 */
	public static <T> List<T> asList(T... array)
	{
		return Arrays.asList(array);
	}
	
	/**
	 * Creates an array from the given {@code collection}
	 * 
	 * @see Collections#toArray(Collection)
	 * @param collection
	 *            the collection
	 * @return the array
	 */
	public static <T> T[] fromList(Collection<? extends T> collection)
	{
		return Collections.toArray(collection);
	}
	
	/**
	 * Creates an array from the given {@code collection}
	 * 
	 * @see Collections#toArray(Class, Collection)
	 * @param type
	 *            the expected type
	 * @param collection
	 *            the collection
	 * @return the array
	 */
	public static <T> T[] fromList(Class<T> type, Collection<? extends T> collection)
	{
		return Collections.toArray(type, collection);
	}
	
	/**
	 * Returns the first index of the {@code object} in the {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param object
	 *            the object
	 * @return the first index of the object in the array
	 */
	public static <T> int indexOf(T[] array, T object)
	{
		return indexOf(array, 0, object);
	}
	
	/**
	 * Returns the first index after {@code start} of the {@code object} in the
	 * {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param start
	 *            the index to start from
	 * @param object
	 *            the object
	 * @return the first index of the object in the array
	 */
	public static <T> int indexOf(T[] array, int start, T object)
	{
		for (int i = start; i < array.length; i++)
		{
			if (Objects.equals(object, array[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the last index of the {@code object} in the {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param object
	 *            the object
	 * @return the last index of the object in the array
	 */
	public static <T> int lastIndexOf(T[] array, T object)
	{
		return lastIndexOf(array, array.length - 1, object);
	}
	
	/**
	 * Returns the last index before {@code start} of the {@code object} in the
	 * {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param start
	 *            the index to start from
	 * @param object
	 *            the object
	 * @return the last index of the object in the array
	 */
	public static <T> int lastIndexOf(T[] array, int start, T object)
	{
		for (int i = start; i >= 0; i--)
		{
			if (Objects.equals(object, array[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the first index of the any of the {@code objects} in the
	 * {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param objects
	 *            the objects
	 * @return the first index of the object in the array
	 */
	public static <T> int indexOfAny(T[] array, T... objects)
	{
		for (Object object : objects)
		{
			int index = indexOf(array, object);
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the last index of any of the {@code objects} in the {@code array}
	 * 
	 * @param array
	 *            the array
	 * @param objects
	 *            the objects
	 * @return the last index of the object in the array
	 */
	public static <T> int lastIndexOfAny(T[] array, T... objects)
	{
		for (Object object : objects)
		{
			int index = lastIndexOf(array, object);
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * Checks if {@code array} contains the {@code object}.
	 * 
	 * @param array
	 *            the array
	 * @param object
	 *            the object
	 * @return true, if the array contains the object
	 */
	public static <T> boolean contains(T[] array, T object)
	{
		return indexOf(array, object) != -1;
	}
	
	/**
	 * Checks if {@code array} contains any of the {@code objects}.
	 * 
	 * @param array
	 *            the array
	 * @param objects
	 *            the objects
	 * @return true, if the array contains any of the objects
	 */
	public static <T> boolean containsAny(T[] array, T... objects)
	{
		for (Object object : objects)
		{
			if (contains(array, object))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if {@code array} contains all {@code objects}.
	 * 
	 * @param array
	 *            the array
	 * @param objects
	 *            the objects
	 * @return true, if the array contains all objects
	 */
	public static <T> boolean containsAll(T[] array, T... objects)
	{
		for (Object object : objects)
		{
			if (!contains(array, object))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Transform an array of a primitive type to an array of its corresponding
	 * wrapper class.
	 * 
	 * @param array
	 *            the array of a primitive type
	 * @return the array of wrapper objects
	 */
	public static Object[] primitiveTransform(Object array)
	{
		Object[] result = (Object[]) array;
		if (array instanceof boolean[])
		{
			boolean[] array1 = (boolean[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Boolean.valueOf(array1[i]);
			}
		}
		else if (array instanceof byte[])
		{
			byte[] array1 = (byte[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Byte.valueOf(array1[i]);
			}
		}
		else if (array instanceof short[])
		{
			short[] array1 = (short[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Short.valueOf(array1[i]);
			}
		}
		else if (array instanceof int[])
		{
			int[] array1 = (int[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Integer.valueOf(array1[i]);
			}
		}
		else if (array instanceof float[])
		{
			float[] array1 = (float[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Float.valueOf(array1[i]);
			}
		}
		else if (array instanceof double[])
		{
			double[] array1 = (double[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Double.valueOf(array1[i]);
			}
		}
		else if (array instanceof long[])
		{
			long[] array1 = (long[]) array;
			result = new Object[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				result[i] = Long.valueOf(array1[i]);
			}
		}
		return result;
	}
	
	/**
	 * Returns the first index of the {@code integer} in the {@code array}
	 * 
	 * @param array
	 *            the int array
	 * @param integer
	 *            the integer
	 * @return the first index of the integer in the array
	 */
	public static int indexOf(int[] array, int integer)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == integer)
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Checks if {@code array} contains the {@code integer}.
	 * 
	 * @param array
	 *            the array
	 * @param integer
	 *            the integer
	 * @return true, if the array contains the integer
	 */
	public static boolean contains(int[] intArray, int integer)
	{
		return indexOf(intArray, integer) != -1;
	}
}
