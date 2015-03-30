package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with an array literal. The process of
 * doing so will cause the compiler to insert a call to a method of that class
 * with the signature {@code static X apply([Y])}, where {@code Y} is the type
 * of the array. Note that such a method is automatically inserted by the
 * <i>Dyvil Compiler</i> for any {@code case class} that takes a single
 * {@code [X]} parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * case class IntVector([int] value) implements ArrayConvertible
 * 
 * // ----------
 * 
 * IntVector vec = [ 1, 2, 3 ]
 * </pre>
 * 
 * @author Clashsoft
 */
public interface ArrayConvertible
{
}
