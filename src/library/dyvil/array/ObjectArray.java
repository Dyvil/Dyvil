package dyvil.array;

import static dyvil.reflect.Opcodes.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.*;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;

public interface ObjectArray
{
	public static final Object[]	EMPTY	= new Object[0];
	
	public static <T> T[] apply()
	{
		return (T[]) EMPTY;
	}
	
	public static <T> T[] apply(Class<T> type, int count)
	{
		return (T[]) Array.newInstance(type, count);
	}
	
	public static <T> T[] apply(Class<T> type, int count, T repeatedValue)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static <T> T[] apply(Class<T> type, int count, IntFunction<T> generator)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.apply(i);
		}
		return array;
	}
	
	// Basic Array Operations
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH })
	public static @infix <T> int length(T[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AALOAD })
	public static @infix <T> T apply(T[] array, int i)
	{
		return array[i];
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AASTORE })
	public static @infix <T> void update(T[] array, int i, T v)
	{
		array[i] = v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	public static @infix <T> void forEach(T[] array, Consumer<T> action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix <T> T[] $plus(T[] array, T v)
	{
		int len = array.length;
		T[] res = (T[]) Array.newInstance(array.getClass().getComponentType(), len + 1);
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	public static @infix <T> T[] $plus$plus(T[] array1, T[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		T[] res = (T[]) Array.newInstance(array1.getClass().getComponentType(), len1 + len2);
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	public static @infix <T> T[] $minus(T[] array, T v)
	{
		int index = indexOf(array, v, 0);
		if (index < 0)
		{
			return array;
		}
		
		int len = array.length;
		T[] res = (T[]) Array.newInstance(array.getClass().getComponentType(), len - 1);
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
	
	public static @infix <T> T[] $minus$minus(T[] array1, T[] array2)
	{
		int index = 0;
		int len = array1.length;
		// We can safely use clone here because no data will be leaked
		T[] res = array1.clone();
		
		for (int i = 0; i < len; i++)
		{
			T v = array1[i];
			if (indexOf(array2, v, 0) < 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix <T> T[] $amp(T[] array1, T[] array2)
	{
		int index = 0;
		int len = array1.length;
		// We can safely use clone here because no data will be leaked
		T[] res = array1.clone();
		
		for (int i = 0; i < len; i++)
		{
			T v = array1[i];
			if (indexOf(array2, v, 0) >= 0)
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix <T> T[] mapped(T[] array, UnaryOperator<T> mapper)
	{
		int len = array.length;
		// We can safely use clone here because no data will be leaked
		T[] res = array.clone();
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.apply(array[i]);
		}
		return res;
	}
	
	public static @infix <T> T[] filtered(T[] array, Predicate<T> condition)
	{
		int index = 0;
		int len = array.length;
		// We can safely use clone here because no data will be leaked
		T[] res = array.clone();
		for (int i = 0; i < len; i++)
		{
			T v = array[i];
			if (condition.test(v))
			{
				res[index++] = v;
			}
		}
		
		// Return a resized copy of the temporary array
		return Arrays.copyOf(res, index);
	}
	
	public static @infix <T> T[] sorted(T[] array)
	{
		T[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	public static @infix <T> T[] sorted(T[] array, Comparator<? super T> comparator)
	{
		T[] res = array.clone();
		Arrays.<T> sort(array, comparator);
		return res;
	}
	
	// Array Creation
	
	public static @infix <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	// Component Types
	
	public static @infix <T> Class<T> getComponentType(T[] array)
	{
		return (Class<T>) array.getClass().getComponentType();
	}
	
	public static @infix <T> Class getDeepComponentType(T[] array)
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
	
	// Operators
	
	public static @infix <T> T[] $plus(T[] a, T[] b)
	{
		int len1 = a.length;
		int len2 = b.length;
		T[] res = (T[]) new Object[len1 + len2];
		System.arraycopy(a, 0, res, 0, len1);
		System.arraycopy(b, 0, res, len1, len2);
		return res;
	}
	
	public static @infix <T> String toString(T[] array)
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
	
	// Search Operations
	
	public static @infix <T> int indexOf(T[] array, T v)
	{
		return indexOf(array, v, 0);
	}
	
	public static @infix <T> int indexOf(T[] array, T v, int start)
	{
		for (; start < array.length; start++)
		{
			if (Objects.equals(v, array[start]))
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix <T> int lastIndexOf(T[] array, T v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	public static @infix <T> int lastIndexOf(T[] array, T v, int start)
	{
		for (; start >= 0; start--)
		{
			if (Objects.equals(v, array[start]))
			{
				return start;
			}
		}
		return -1;
	}
	
	public static @infix <T> boolean contains(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	// Copying
	
	public static @infix <T> T[] copy(T[] array)
	{
		return array.clone();
	}
	
	public static @infix <T> T[] copy(T[] array, int newLength)
	{
		return (T[]) java.util.Arrays.copyOf(array, newLength, array.getClass());
	}
	
	public static @infix <T, N> N[] copy(T[] array, int newLength, Class<? extends N[]> newType)
	{
		return java.util.Arrays.<N, T> copyOf(array, newLength, newType);
	}
}
