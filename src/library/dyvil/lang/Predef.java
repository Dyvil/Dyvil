package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.implicit;
import dyvil.lang.tuple.Tuple2;

public class Predef
{
	private Predef()
	{
	}
	
	// Prefix Operators
	
	@Bytecode
	public static int $plus(int i)
	{
		return i;
	}
	
	@Bytecode(postfixOpcode = INEG)
	public static int $minus(int i)
	{
		return -i;
	}
	
	@Bytecode(postfixOpcodes = { ICONST_M1, IXOR })
	public static int $tilde(int i)
	{
		return ~i;
	}
	
	@Bytecode
	public static long $plus(long l)
	{
		return l;
	}
	
	@Bytecode(postfixOpcode = LNEG)
	public static long $minus(long l)
	{
		return -l;
	}
	
	@Bytecode(postfixOpcodes = { LCONST_M1, LXOR })
	public static long $tilde(long l)
	{
		return ~l;
	}
	
	@Bytecode
	public static float $plus(float f)
	{
		return f;
	}
	
	@Bytecode(postfixOpcode = LNEG)
	public static float $minus(float f)
	{
		return -f;
	}
	
	@Bytecode
	public static double $plus(double d)
	{
		return d;
	}
	
	@Bytecode(postfixOpcode = LNEG)
	public static double $minus(double d)
	{
		return -d;
	}
	
	// Hashing
	
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
	@Bytecode(postfixOpcode = INSTANCEOF)
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
	@Bytecode(postfixOpcode = CHECKCAST)
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
	@Bytecode(postfixOpcode = CHECKCAST)
	public static @implicit <T, U> U $colon$greater(T t, Class<U> c)
	{
		return t == null ? (U) null : c.cast(t);
	}
	
	// Miscellaneous
	
	public static void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException();
	}
	
	@Bytecode
	public static void $dot$dot$dot()
	{
	}
}
