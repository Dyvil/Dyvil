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
	
	// Object Operators
	
	@Bytecode(postfixOpcode = IF_ACMPNE)
	public static boolean $eq$eq(Object o1, Object o2)
	{
		return o1 == o2;
	}
	
	@Bytecode(postfixOpcode = IF_ACMPEQ)
	public static boolean $bang$eq(Object o1, Object o2)
	{
		return o1 != o2;
	}
	
	// Prefix Operators
	
	@Bytecode(postfixOpcode = IBIN)
	public static boolean $bang(boolean b)
	{
		return !b;
	}
	
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
	
	@Bytecode(postfixOpcode = IBIN)
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
	
	@Bytecode(postfixOpcode = LBIN)
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
		return o == null ? 0 : o.hashCode();
	}
	
	public static @implicit int $hash$hash(int i)
	{
		return i;
	}
	
	public static @implicit int $hash$hash(long l)
	{
		return (int) (int) (l ^ (l >>> 32));
	}
	
	public static @implicit int $hash$hash(float f)
	{
		return java.lang.Float.hashCode(f);
	}
	
	public static @implicit int $hash$hash(double d)
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
	
	public static @implicit void match(Object o, Pattern[] patterns)
	{
		for (Pattern p : patterns)
		{
			if (p.match(o))
			{
				break;
			}
		}
	}
	
	public static void $qmark$qmark$qmark()
	{
		throw new UnsupportedOperationException();
	}
	
	@Bytecode
	public static void $dot$dot$dot()
	{
	}
}
