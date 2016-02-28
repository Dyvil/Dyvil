package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a colon operator. This will cause the compiler to insert a call to a
 * method of that class with the signature {@code static X apply(Y1, Y2)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class X} that takes {@code 2} parameters, as shown in the
 * below example.
 * <p>
 * Example:
 * <p>
 * <pre>
 * {@literal @}ColonConvertible
 * case class Point(int x, int y)
 *
 * // generated because this is a case class
 * // public static Point apply(int x, int y) = new Point(x, y)
 *
 * // ----------
 *
 * Point p = 10 : 20 // generates a call to Point.apply(10, 20)
 * </pre>
 *
 * @author Clashsoft
 */
public @interface ColonConvertible
{
	String methodName() default "apply";
}
