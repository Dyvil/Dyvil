package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a tuple literal. The process of
 * doing so will cause the compiler to insert a call to a method of that class
 * with the signature {@code static X apply(Y1, Y2, ... Yi)}, where {@code Yi}
 * is the {@code i}th type of the a tuple with {@code N} types. Note that such a
 * method is automatically inserted by the <i>Dyvil Compiler</i> for any
 * {@code case class X} that takes {@code N} parameters, as shown in the below
 * example.
 * <p>
 * Example:
 * 
 * <pre>
 * case class Point(int x, int y) implements TupleConvertible
 * 
 * // generated because this is a case class
 * // public static Point apply(int x, int y) = new Point(x, y)
 * 
 * // ----------
 * 
 * Point p = ( 10, 20 ) // generates a call to Point.apply(10, 20)
 * </pre>
 * 
 * @author Clashsoft
 */
public interface TupleConvertible
{
}
