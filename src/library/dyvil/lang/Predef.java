package dyvil.lang;

import static dyvil.reflect.Opcodes.*;

import java.util.*;

import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.infix;
import dyvil.lang.annotation.inline;
import dyvil.lang.tuple.Tuple2;

public final class Predef
{
	private Predef()
	{
	}
	
	// Object Operators
	
	@Intrinsic({ INSTANCE, ARGUMENTS, OBJECT_EQUALS, IFNE })
	public static @infix boolean $eq$eq(Object o1, Object o2)
	{
		return o1.equals(o2);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ACMPEQ })
	public static @infix boolean $eq$eq$eq(Object o1, Object o2)
	{
		return o1 == o2;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, OBJECT_EQUALS, IFEQ })
	public static @infix boolean $bang$eq(Object o1, Object o2)
	{
		return !o1.equals(o2);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ACMPNE })
	public static @infix boolean $bang$eq$eq(Object o1, Object o2)
	{
		return o1 != o2;
	}
	
	// String Concatenation
	
	public static @infix @inline String $plus(String s, Object o)
	{
		return s + o;
	}
	
	public static @infix @inline String $plus(String s, boolean v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, byte v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, short v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, char v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, int v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, long v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, float v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(String s, double v)
	{
		return s + v;
	}
	
	public static @infix @inline String $plus(Object o, String s)
	{
		return o + s;
	}
	
	public static @infix @inline String $plus(boolean v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(byte v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(short v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(char v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(int v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(long v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(float v, String s)
	{
		return v + s;
	}
	
	public static @infix @inline String $plus(double v, String s)
	{
		return v + s;
	}
	
	// Hashing
	
	public static int hash(Object... args)
	{
		if (args == null)
		{
			return 0;
		}
		
		int result = 1;
		
		for (Object element : args)
		{
			result = 31 * result + (element == null ? 0 : element.hashCode());
		}
		
		return result;
	}
	
	public static @inline @infix int $hash$hash(Object o)
	{
		return o == null ? 0 : o.hashCode();
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS })
	public static @infix int $hash$hash(int i)
	{
		return i;
	}
	
	public static @inline @infix int $hash$hash(long l)
	{
		return (int) (l ^ l >>> 32);
	}
	
	public static @inline @infix int $hash$hash(float f)
	{
		return java.lang.Float.hashCode(f);
	}
	
	public static @inline @infix int $hash$hash(double d)
	{
		return java.lang.Double.hashCode(d);
	}
	
	// Print
	
	public static @inline void println()
	{
		System.out.println();
	}
	
	public static @inline void println(String s)
	{
		System.out.println(s);
	}
	
	// Tuples
	
	/**
	 * @dyvil ->
	 * @param a
	 * @param b
	 * @return
	 */
	public static @inline @infix <A, B> Tuple2<A, B> $minus$gt(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	/**
	 * @dyvil <-
	 * @param a
	 * @param b
	 * @return
	 */
	public static @inline @infix <A, B> Tuple2<B, A> $lt$minus(A a, B b)
	{
		return new Tuple2(b, a);
	}
	
	// Miscellaneous
	
	public static @inline List<?> List()
	{
		return new ArrayList();
	}
	
	public static <T> List<T> List(T e1)
	{
		ArrayList<T> list = new ArrayList(1);
		list.add(e1);
		return list;
	}
	
	public static <T> List<T> List(T e1, T e2)
	{
		ArrayList<T> list = new ArrayList(2);
		list.add(e1);
		list.add(e2);
		return list;
	}
	
	public static <T> List<T> List(T e1, T e2, T e3)
	{
		ArrayList<T> list = new ArrayList(3);
		list.add(e1);
		list.add(e2);
		list.add(e3);
		return list;
	}
	
	public static <T> List<T> List(T... elements)
	{
		int len = elements.length;
		ArrayList<T> list = new ArrayList(len);
		for (int i = 0; i < len; i++)
		{
			list.add(elements[i]);
		}
		return list;
	}
	
	public static @inline Set<?> Set()
	{
		return new HashSet();
	}
	
	public static <T> Set<T> Set(T e1)
	{
		Set<T> set = new HashSet(1);
		set.add(e1);
		return set;
	}
	
	public static <T> Set<T> Set(T e1, T e2)
	{
		Set<T> set = new HashSet(2);
		set.add(e1);
		set.add(e2);
		return set;
	}
	
	public static <T> Set<T> Set(T e1, T e2, T e3)
	{
		Set<T> set = new HashSet(3);
		set.add(e1);
		set.add(e2);
		set.add(e3);
		return set;
	}
	
	public static <T> Set<T> Set(T... elements)
	{
		int len = elements.length;
		Set<T> set = new HashSet(len);
		for (int i = 0; i < len; i++)
		{
			set.add(elements[i]);
		}
		return set;
	}
	
	public static @inline Map<?, ?> Map()
	{
		return new HashMap();
	}
	
	public static <K, V> Map<K, V> Map(Tuple2<K, V> e1)
	{
		Map<K, V> map = new HashMap<K, V>(1);
		map.put(e1._1, e1._2);
		return map;
	}
	
	public static <K, V> Map<K, V> Map(Tuple2<K, V> e1, Tuple2<K, V> e2)
	{
		Map<K, V> map = new HashMap<K, V>(2);
		map.put(e1._1, e1._2);
		map.put(e2._1, e2._2);
		return map;
	}
	
	public static <K, V> Map<K, V> Map(Tuple2<K, V> e1, Tuple2<K, V> e2, Tuple2<K, V> e3)
	{
		Map<K, V> map = new HashMap<K, V>(1);
		map.put(e1._1, e1._2);
		map.put(e2._1, e2._2);
		map.put(e3._1, e3._2);
		return map;
	}
	
	public static <K, V> Map<K, V> Map(Tuple2<K, V>... entries)
	{
		int len = entries.length;
		Map<K, V> map = new HashMap<K, V>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple2<K, V> entry = entries[i];
			map.put(entry._1, entry._2);
		}
		return map;
	}
	
	public static @inline void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException();
	}
	
	@Intrinsic({})
	public static @inline void $dot$dot$dot()
	{
	}
}
