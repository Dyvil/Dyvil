package dyvil.array;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.Mutating;
import dyvil.annotation.Reified;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Range;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.range.Rangeable;
import dyvil.ref.ObjectRef;
import dyvil.ref.array.ObjectArrayRef;
import dyvil.reflect.Modifiers;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static dyvil.reflect.Opcodes.*;

public interface ObjectArray
{
	Object[] EMPTY = new Object[0];
	
	static <T> T[] apply()
	{
		return (T[]) EMPTY;
	}
	
	static <@Reified(erasure = true) T> T[] apply(int count, Class<T> type)
	{
		return (T[]) Array.newInstance(type, count);
	}
	
	static <@Reified(erasure = true) T> T[] repeat(int count, T repeatedValue, Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = repeatedValue;
		}
		return array;
	}
	
	static <@Reified(erasure = true) T> T[] generate(int count, IntFunction<T> generator, Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, count);
		for (int i = 0; i < count; i++)
		{
			array[i] = generator.apply(i);
		}
		return array;
	}
	
	static <@Reified(erasure = true) T extends Rangeable<T>> T[] range(T start, T end, Class<T> type)
	{
		int i = 0;
		T[] array = (T[]) Array.newInstance(type, start.distanceTo(end) + 1);
		for (T current = start; current.compareTo(end) <= 0; current = current.next())
		{
			array[i++] = current;
		}
		return array;
	}
	
	static <@Reified(erasure = true) T extends Rangeable<T>> T[] rangeOpen(T start, T end, Class<T> type)
	{
		int i = 0;
		T[] array = (T[]) Array.newInstance(type, start.distanceTo(end));
		for (T current = start; current.compareTo(end) < 0; current = current.next())
		{
			array[i++] = current;
		}
		return array;
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH })
	@DyvilModifiers(Modifiers.INFIX)
	static <T> int length(T[] array)
	{
		return array.length;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, AALOAD })
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T subscript(T[] array, int i)
	{
		return array[i];
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] subscript(T[] array, Range<Integer> range)
	{
		int start = (range.first());
		int count = range.count();
		T[] slice = (T[]) Array.newInstance(array.getClass().getComponentType(), count);
		System.arraycopy(array, start, slice, 0, count);
		return slice;
	}
	
	@Intrinsic( { LOAD_0, LOAD_1, LOAD_2, AASTORE })
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static <T> void subscript_$eq(T[] array, int i, T v)
	{
		array[i] = v;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static <T> void subscript_$eq(T[] array, Range<Integer> range, T[] values)
	{
		int start = (range.first());
		int count = range.count();
		System.arraycopy(values, 0, array, start, count);
	}

	@DyvilModifiers(Modifiers.INFIX)
	@Mutating
	static <T> ObjectRef<T> subscriptRef(T[] array, int index)
	{
		return new ObjectArrayRef<>(array, index);
	}
	
	@Intrinsic( { LOAD_0, ARRAYLENGTH, EQ0 })
	@DyvilModifiers(Modifiers.INFIX)
	static boolean isEmpty(int[] array)
	{
		return array.length == 0;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> void forEach(T[] array, Consumer<? super T> action)
	{
		for (T v : array)
		{
			action.accept(v);
		}
	}
	
	// Operators
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean $qmark(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean $eq$eq(T[] array1, T[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean $bang$eq(T[] array1, T[] array2)
	{
		return !Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] $plus(T[] array, T v)
	{
		int len = array.length;
		T[] res = (T[]) Array.newInstance(array.getClass().getComponentType(), len + 1);
		System.arraycopy(array, 0, res, 0, len);
		res[len] = v;
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] $plus$plus(T[] array1, T[] array2)
	{
		int len1 = array1.length;
		int len2 = array2.length;
		T[] res = (T[]) Array.newInstance(array1.getClass().getComponentType(), len1 + len2);
		System.arraycopy(array1, 0, res, 0, len1);
		System.arraycopy(array2, 0, res, len1, len2);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] $minus(T[] array, T v)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] $minus$minus(T[] array1, T[] array2)
	{
		int index = 0;
		// We can safely use clone here because no data will be leaked
		T[] res = array1.clone();
		
		for (T v : array1)
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
	static <T> T[] $amp(T[] array1, T[] array2)
	{
		int index = 0;
		// We can safely use clone here because no data will be leaked
		T[] res = array1.clone();
		
		for (T v : array1)
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
	static <T, @Reified(erasure = true) U> U[] mapped(T[] array, Function<T, U> mapper, Class<U> type)
	{
		int len = array.length;
		U[] res = (U[]) Array.newInstance(type, len);
		for (int i = 0; i < len; i++)
		{
			res[i] = mapper.apply(array[i]);
		}
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T, @Reified(erasure = true) U> U[] flatMapped(T[] array, Function<T, U[]> mapper, Class<U> type)
	{
		int size = 0;
		U[] res = (U[]) EMPTY;
		
		for (T v : array)
		{
			U[] a = mapper.apply(v);
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] filtered(T[] array, Predicate<T> condition)
	{
		int index = 0;
		// We can safely use clone here because no data will be leaked
		T[] res = array.clone();
		for (T v : array)
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
	static <T> T[] sorted(T[] array)
	{
		T[] res = array.clone();
		Arrays.sort(res);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] sorted(T[] array, Comparator<? super T> comparator)
	{
		T[] res = array.clone();
		Arrays.sort(array, comparator);
		return res;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] newArray(Class<T> type, int size)
	{
		return (T[]) Array.newInstance(type, size);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> Class<T> getComponentType(T[] array)
	{
		return (Class<T>) array.getClass().getComponentType();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> Class getDeepComponentType(T[] array)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> Class<T[]> getArrayType(Class<T> componentType)
	{
		return (Class<T[]>) Array.newInstance(componentType, 0).getClass();
	}
	
	// Search Operations
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> int indexOf(T[] array, T v)
	{
		return indexOf(array, v, 0);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> int indexOf(T[] array, T v, int start)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> int lastIndexOf(T[] array, T v)
	{
		return lastIndexOf(array, v, array.length - 1);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> int lastIndexOf(T[] array, T v, int start)
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
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean contains(T[] array, T v)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean in(T v, T[] array)
	{
		return indexOf(array, v, 0) != -1;
	}
	
	// Copying
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] copy(T[] array)
	{
		return array.clone();
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> T[] copy(T[] array, int newLength)
	{
		return copy(array, newLength, (Class<T>) array.getClass().getComponentType());
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T extends N, N> N[] copy(T[] array, int newLength, Class<N> type)
	{
		N[] newArray = (N[]) Array.newInstance(type, newLength);
		System.arraycopy(array, 0, newArray, 0, newLength);
		return newArray;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static boolean[] unboxed(Boolean[] array)
	{
		int len = array.length;
		boolean[] unboxed = new boolean[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = array[i];
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static byte[] unboxed(Byte[] array)
	{
		int len = array.length;
		byte[] unboxed = new byte[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static short[] unboxed(Short[] array)
	{
		int len = array.length;
		short[] unboxed = new short[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static char[] unboxed(Character[] array)
	{
		int len = array.length;
		char[] unboxed = new char[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static int[] unboxed(Integer[] array)
	{
		int len = array.length;
		int[] unboxed = new int[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static long[] unboxed(Long[] array)
	{
		int len = array.length;
		long[] unboxed = new long[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static float[] unboxed(Float[] array)
	{
		int len = array.length;
		float[] unboxed = new float[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static double[] unboxed(Double[] array)
	{
		int len = array.length;
		double[] unboxed = new double[len];
		for (int i = 0; i < len; i++)
		{
			unboxed[i] = (array[i]);
		}
		return unboxed;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> Iterable<T> toIterable(T[] array)
	{
		return new ArrayList<>(array, true);
	}
	
	// toString, equals and hashCode
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean equals(T[] array1, T[] array2)
	{
		return Arrays.equals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> boolean deepEquals(T[] array1, T[] array2)
	{
		return Arrays.deepEquals(array1, array2);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> int hashCode(T[] array)
	{
		return Arrays.hashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> int deepHashCode(T[] array)
	{
		return Arrays.deepHashCode(array);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	static <T> String toString(T[] array)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static void toString(Object[] array, StringBuilder builder)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static String deepToString(Object[] array)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static void deepToString(Object[] array, StringBuilder builder)
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
	
	@DyvilModifiers(Modifiers.INFIX)
	static void toString(Object o, StringBuilder builder)
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
