package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.infix;
import dyvil.lang.tuple.Tuple2;

public final class Predef
{
	private Predef()
	{
	}
	
	// Object Operators
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ACMPEQ })
	public static boolean $eq$eq(Object o1, Object o2)
	{
		return o1 == o2;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ACMPNE })
	public static boolean $bang$eq(Object o1, Object o2)
	{
		return o1 != o2;
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
	
	public static @infix int $hash$hash(Object o)
	{
		return o == null ? 0 : o.hashCode();
	}
	
	public static @infix int $hash$hash(int i)
	{
		return i;
	}
	
	public static @infix int $hash$hash(long l)
	{
		return (int) (l ^ l >>> 32);
	}
	
	public static @infix int $hash$hash(float f)
	{
		return java.lang.Float.hashCode(f);
	}
	
	public static @infix int $hash$hash(double d)
	{
		return java.lang.Double.hashCode(d);
	}
	
	// Print
	
	public static void println()
	{
		System.out.println();
	}
	
	public static void println(String s)
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
	public static @infix <A, B> Tuple2<A, B> $minus$greater(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	/**
	 * @dyvil <-
	 * @param a
	 * @param b
	 * @return
	 */
	public static @infix <A, B> Tuple2<B, A> $less$minus(A a, B b)
	{
		return new Tuple2(b, a);
	}
	
	// Casts
	
	/**
	 * Returns true if the given {@code T t} is an instance of the given
	 * {@link Class} {@code c}.
	 * 
	 * @dyvil <:
	 * @param t
	 *            the object
	 * @param c
	 *            the class
	 * @return true, if t is an instance of c
	 */
	public static @infix <T> boolean $less$colon(T t, Class<?> c)
	{
		return t == null ? false : c.isInstance(t);
	}
	
	/**
	 * Casts the given {@code T t} to the given {@link Class} {@code c} of type
	 * {@code U}.
	 * 
	 * @dyvil :>
	 * @param t
	 *            the object
	 * @param c
	 *            the class
	 * @return t as an instance of c
	 */
	public static @infix <T, U> U $colon$greater(T t, Class<U> c)
	{
		return t == null ? (U) null : c.cast(t);
	}
	
	// Miscellaneous
	
	public static @infix Object match(Object o, Pattern[] patterns)
	{
		Option res;
		for (Pattern p : patterns)
		{
			res = p.match(o);
			if (res.isDefined())
			{
				return res.get();
			}
		}
		throw new MatchError(o);
	}
	
	public static void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException();
	}
	
	@Intrinsic({ NOP })
	public static void $dot$dot$dot()
	{
	}
}
