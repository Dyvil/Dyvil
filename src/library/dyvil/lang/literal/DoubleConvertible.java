package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a double literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(double)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a
 * single {@code double} parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}DoubleConvertible
 * case class Percentage(double value)
 * {
 *     public int AsInt {
 *         get: ...
 *         set: ...
 *     }
 * }
 * 
 * // ----------
 * 
 * Percentage p = 100.0
 * int i = p.AsInt
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface DoubleConvertible
{
	String methodName() default "apply";
}
