package dyvil.lang;

import dyvil.lang.annotation.implicit;
import dyvil.lang.tuple.Tuple2;

public class Predef
{
	public static int hash(Object o)
	{
		return o == null ? 0 : o.hashCode();
	}
	
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
	
	public static @implicit int $hash$hash(Object o)
	{
		return hash(o);
	}
	
	/**
	 * @dyvil ->
	 * @param a
	 * @param b
	 * @return
	 */
	public static @implicit <A, B> Tuple2<A, B> $minus$greater(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	/**
	 * @dyvil <-
	 * @param a
	 * @param b
	 * @return
	 */
	public static @implicit <A, B> Tuple2<B, A> $less$minus(A a, B b)
	{
		return new Tuple2(b, a);
	}
	
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
	public static @implicit <T> boolean $less$colon(T t, Class c)
	{
		return t == null ? false : c.isInstance(t);
	}
	
	/**
	 * Casts the given {@code T t} to the given type {@code U}.
	 * 
	 * @dyvil :>
	 * @param t
	 *            the object
	 * @param c
	 *            the class
	 * @return t as an instance of c
	 */
	public static @implicit <T, U> U $colon$greater(T t)
	{
		return (U) t;
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
	public static @implicit <T, U> U $colon$greater(T t, Class<U> c)
	{
		return t == null ? (U) null : c.cast(t);
	}
	
	public static @implicit java.lang.String $plus(Object o, String s)
	{
		return java.lang.String.valueOf(o) + s;
	}
}
