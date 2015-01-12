package dyvil.lang;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.implicit;
import dyvil.lang.tuple.Tuple2;

public class Predef
{
	private Predef()
	{
	}
	
	public static void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException();
	}
	
	@Bytecode
	public static void $dot$dot$dot()
	{
	}
	
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
	
	public static void println()
	{
		System.out.println();
	}
	
	public static void println(String s)
	{
		System.out.println(s);
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
	@Bytecode(postfixOpcode = Opcodes.INSTANCEOF)
	public static @implicit <T> boolean $less$colon(T t, Class<?> c)
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
	@Bytecode(postfixOpcode = Opcodes.CHECKCAST)
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
	@Bytecode(postfixOpcode = Opcodes.CHECKCAST)
	public static @implicit <T, U> U $colon$greater(T t, Class<U> c)
	{
		return t == null ? (U) null : c.cast(t);
	}
	
	public static @implicit java.lang.String $plus(java.lang.String s1, java.lang.String s2)
	{
		return s1 + s2;
	}
	
	public static @implicit java.lang.String $plus(Object o, java.lang.String s)
	{
		return o + s;
	}
}
