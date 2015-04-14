package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
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
	
	public static @inline void println(Object o)
	{
		if (o == null)
		{
			System.out.println("null");
			return;
		}
		System.out.println(o);
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
