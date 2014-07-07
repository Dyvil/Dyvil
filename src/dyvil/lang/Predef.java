package dyvil.lang;

import dyvil.lang.annotation.implicit;
import dyvil.lang.tuple.Tuple2;

public class Predef
{
	/**
	 * @dyvil ->
	 * @param a
	 * @param b
	 * @return
	 */
	public static @implicit <A, B> Tuple2<A, B> $arr(A a, B b)
	{
		return new Tuple2(a, b);
	}
	
	/**
	 * @dyvil <-
	 * @param a
	 * @param b
	 * @return
	 */
	public static @implicit <A, B> Tuple2<B, A> $arl(A a, B b)
	{
		return new Tuple2(b, a);
	}
	
	/**
	 * Returns true if the given {@code T t} is an instance of the given
	 * {@link Class} {@code c}.
	 * 
	 * @dyvil :>
	 * @param t
	 *            the object
	 * @param c
	 *            the class
	 * @return true, if t is an instance of c
	 */
	public static @implicit <T> boolean $iof(T t, Class c)
	{
		return t == null ? false : c.isAssignableFrom(t.getClass());
	}
	
	/**
	 * Casts the given {@code T t} to the given {@link Class} {@code c}.
	 * 
	 * @dyvil =>
	 * @param t
	 *            the object
	 * @param c
	 *            the class
	 * @return t as an instance of c
	 */
	public static @implicit <T, U> U $cst(T t, Class<U> c)
	{
		return t == null ? (U) null : c.cast(t);
	}
}
