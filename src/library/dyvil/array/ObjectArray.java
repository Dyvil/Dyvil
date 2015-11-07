package dyvil.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import dyvil.lang.Boolean;
import dyvil.lang.Byte;
import dyvil.lang.Char;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Int;
import dyvil.lang.Long;
import dyvil.lang.Rangeable;
import dyvil.lang.Short;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Reified;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;

import static dyvil.reflect.Opcodes.*;

public interface ObjectArray
{
	public static final Object[] EMPTY = new Object[0];
	
	public static <T> T[] apply()
	{
		return (T[]) EMPTY;
	}
	
	public static <@Reified T> T[] apply(int count, Class<T> type)
	{
		return (T[]) Array.newInstance(type, count);
	}
	
	public static <@Reified T> T[] repeat(int count, T repeatedValue, Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	public static <@Reified T> T[] generate(int count, IntFunction<T> generator, Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.apply(i);
		}
		return array;
	}
	
	public static <@Reified T extends Rangeable<T>> T[] range(T start, T end, Class<T> type)
	{
		int i = 0;
		T[] array = (T[]) Array.newInstance(type, start.distanceTo(end) + 1);
		for (T current = start; current.$lt$eq(end); current = current.next())
		{
			array[i++] = current;
		}
		return array;
	}
	
	public static <@Reified T extends Rangeable<T>> T[] rangeOpen(T start, T end, Class<T> type)
	{
		int i = 0;
		T[] array = (T[]) Array.newInstance(type, start.distanceTo(end));
		for (T current = start; current.$lt(end); current = current.next())
		{
			array[i++] = current;
		}
		return array;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH })
	public static @infix <T> int length(T[] array)
	{
		return array.length;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, AALOAD })
	public static @infix <T> T subscript(T[] array, int i)
	{
		return array[i];
	}
	
	public static @infix <T> T[] subscript(T[] array, Range<Int> range)
	{
		int start = Int.unapply(range.first());
		int count = Int.unapply(range.last()) - start + 1;
		T[] slice = (T[]) Array.newInstance(array.getClass().getComponentType(), count);
		for (int i = 0; i < count; i++)
		{
			slice[i] = array[start + i];
		}
		return slice;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, AASTORE })
	public static @infix <T> void subscript_$eq(T[] array, int i, T v)
	{
		array[i] = v;
	}
	
	public static @infix <T> void subscript_$eq(T[] array, Range<Int> range, T[] values)
	{
		int start = Int.unapply(range.first());
		int count = Int.unapply(range.last()) - start + 1;
		for (int i = 0; i < count; i++)
		{
			array[start + i] = values[i];
		}
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, ARRAYLENGTH, IFEQ })
	public static @infix boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	public static @infix <T> void forEach(T[] array, Consumer<? super T> action)
	{
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			action.accept(array[i]);
		}
	}
	
	// Operators
	
	public static @infix @inline <T> boolean $qmark(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	public static @infix @inline <T> boolean $eq$eq(T[] array1, T[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline <T> boolean $bang$eq(T[] array1, T[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
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
	
	public static @infix <T, @Reified U> U[] mapped(T[] array, Function<T, U> mapper, Class<U> type)
	{
		int len = array.length;
		U[] res = (U[]) Array.newInstance(type, len);
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.apply(array[i]);
		}
		return res;
	}
	
	public static @infix <T, @Reified U> U[] flatMapped(T[] array, Function<T, U[]> mapper, Class<U> type)
	{
		int len = array.length;
		int size = 0;
		U[] res = (U[]) EMPTY;
		
		for (int i = 0; i < len; i++)
		{
			U[] a = mapper.apply(array[i]);
			int alen = a.length;
			if (size + alen >= res.length)
			{
				U[] newRes = (U[]) Array.newInstance(type, size + alen);
				System.arraycopy(res, 0, newRes, 0, res.length);
				res = newRes;
			}
			
			System.arraycopy(a, 0, res, size, alen);
			size += alen;
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
	
	public static @infix <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	public static @infix @inline <T> Class<T> getComponentType(T[] array)
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
	
	public static @infix <T> Class<T[]> getArrayType(Class<T> componentType)
	{
		return (Class<T[]>) Array.newInstance(componentType, 0).getClass();
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
	
	public static @infix @inline <T> boolean contains(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	public static @infix @inline <T> boolean in(T v, T[] array)
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
		return copy(array, newLength, (Class<T>) array.getClass().getComponentType());
	}
	
	public static @infix <T, N> N[] copy(T[] array, int newLength, Class<N> type)
	{
		N[] newArray = (N[]) Array.newInstance(type, newLength);
		System.arraycopy(array, 0, newArray, 0, newLength);
		return newArray;
	}
	
	public static @infix boolean[] unboxed(Boolean[] array)
	{
		int len = array.length;
		boolean[] unboxed = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Boolean.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix byte[] unboxed(Byte[] array)
	{
		int len = array.length;
		byte[] unboxed = new byte[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Byte.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix short[] unboxed(Short[] array)
	{
		int len = array.length;
		short[] unboxed = new short[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Short.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix char[] unboxed(Char[] array)
	{
		int len = array.length;
		char[] unboxed = new char[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Char.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix int[] unboxed(Int[] array)
	{
		int len = array.length;
		int[] unboxed = new int[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Int.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix long[] unboxed(Long[] array)
	{
		int len = array.length;
		long[] unboxed = new long[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Long.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix float[] unboxed(Float[] array)
	{
		int len = array.length;
		float[] unboxed = new float[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Float.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix double[] unboxed(Double[] array)
	{
		int len = array.length;
		double[] unboxed = new double[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = Double.unapply(array[i]);
		}
		return unboxed;
	}
	
	public static @infix <T> Iterable<T> toIterable(T[] array)
	{
		return new ArrayList<T>(array, true);
	}
	
	// toString, equals and hashCode
	
	public static @infix @inline <T> boolean equals(T[] array1, T[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	public static @infix @inline <T> boolean deepEquals(T[] array1, T[] array2)
	{
		return Arrays.deepEquals(array1, array2);
	}
	
	public static @infix @inline <T> int hashCode(T[] array)
	{
		return Arrays.hashCode(array);
	}
	
	public static @infix @inline <T> int deepHashCode(T[] array)
	{
		return Arrays.deepHashCode(array);
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
		
		StringBuilder buf = new StringBuilder(len * 10);
		buf.append('[').append(array[0]);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			buf.append(array[i]);
		}
		return buf.append(']').toString();
	}
	
	public static @infix void toString(Object[] array, StringBuilder builder)
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
	
	public static @infix String deepToString(Object[] array)
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
		
		StringBuilder buf = new StringBuilder(len * 10);
		buf.append('[');
		toString(array[0], buf);
		for (int i = 1; i < len; i++)
		{
			buf.append(", ");
			toString(array[i], buf);
		}
		return buf.append(']').toString();
	}
	
	public static @infix void deepToString(Object[] array, StringBuilder builder)
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
		
		builder.append('[');
		toString(array[0], builder);
		for (int i = 1; i < len; i++)
		{
			builder.append(", ");
			toString(array[i], builder);
		}
		builder.append(']');
	}
	
	public static @infix void toString(Object o, StringBuilder builder)
	{
		if (o == null)
		{
			builder.append("null");
			return;
		}
		
		Class c = o.getClass();
		if (c == String.class)
		{
			builder.append((String) o);
			return;
		}
		if (c == boolean[].class)
		{
			BooleanArray.toString((boolean[]) o, builder);
			return;
		}
		if (c == byte[].class)
		{
			ByteArray.toString((byte[]) o, builder);
			return;
		}
		if (c == short[].class)
		{
			ShortArray.toString((short[]) o, builder);
			return;
		}
		if (c == char[].class)
		{
			CharArray.toString((char[]) o, builder);
			return;
		}
		if (c == int[].class)
		{
			IntArray.toString((int[]) o, builder);
			return;
		}
		if (c == long[].class)
		{
			LongArray.toString((long[]) o, builder);
			return;
		}
		if (c == float[].class)
		{
			FloatArray.toString((float[]) o, builder);
			return;
		}
		if (c == double[].class)
		{
			DoubleArray.toString((double[]) o, builder);
			return;
		}
		if (c.isArray())
		{
			deepToString((Object[]) o, builder);
			return;
		}
		
		builder.append(o.toString());
	}
}
