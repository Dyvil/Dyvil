package dyvil.lang;

import java.util.Iterator;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.array.*;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.tuple.Tuple2;

import static dyvil.reflect.Opcodes.*;

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
	
	// Lists
	
	public static @infix <E> ImmutableList<? extends E> $colon$colon(E element, List<? extends E> list)
	{
		return ImmutableList.apply(element).$plus$plus(list);
	}
	
	public static @infix <E> ImmutableList<? extends E> $colon$colon(E element1, E element2)
	{
		return ImmutableList.apply(element1, element2);
	}
	
	public static @infix <E> Iterable<E> toIterable(E... array)
	{
		return ImmutableList.apply(array);
	}
	
	// Strings
	
	public static @infix @inline void toString(Object o, StringBuilder builder)
	{
		builder.append(o == null ? "null" : o.toString());
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
	
	public static @inline void println(boolean v)
	{
		System.out.println(v ? "true" : "false");
	}
	
	public static @inline void println(byte v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(short v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(char v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(int v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(long v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(float v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(double v)
	{
		System.out.println(v);
	}
	
	public static @inline void println(String s)
	{
		System.out.println(s);
	}
	
	public static void println(Object o)
	{
		if (o == null)
		{
			System.out.println("null");
			return;
		}
		System.out.println(o.toString());
	}
	
	public static @inline void println(boolean... v)
	{
		System.out.println(BooleanArray.toString(v));
	}
	
	public static @inline void println(byte... v)
	{
		System.out.println(ByteArray.toString(v));
	}
	
	public static @inline void println(short... v)
	{
		System.out.println(ShortArray.toString(v));
	}
	
	public static @inline void println(char... v)
	{
		System.out.println(CharArray.toString(v));
	}
	
	public static @inline void println(int... v)
	{
		System.out.println(IntArray.toString(v));
	}
	
	public static @inline void println(long... v)
	{
		System.out.println(LongArray.toString(v));
	}
	
	public static @inline void println(float... v)
	{
		System.out.println(FloatArray.toString(v));
	}
	
	public static @inline void println(double... v)
	{
		System.out.println(DoubleArray.toString(v));
	}
	
	public static @inline void println(Object... v)
	{
		System.out.println(ObjectArray.deepToString(v));
	}
	
	public static <T> void println(Iterable<T> iterable)
	{
		Iterator<T> iterator = iterable.iterator();
		if (!iterator.hasNext())
		{
			return;
		}
		
		System.out.print('[');
		System.out.print(iterator.next());
		while (iterator.hasNext())
		{
			System.out.print(", ");
			System.out.print(iterator.next());
		}
		System.out.println(']');
	}
	
	// Tuples
	
	public static @inline @infix <A, B> Tuple2<A, B> $minus$gt(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	public static @inline @infix <A, B> Tuple2<B, A> $lt$minus(A a, B b)
	{
		return new Tuple2(b, a);
	}
	
	public static @inline void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException("Not Implemented!");
	}
}
