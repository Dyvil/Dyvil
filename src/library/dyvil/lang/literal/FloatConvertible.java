package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a float literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(float)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a
 * single {@code float} parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}FloatConvertible
 * case class Percentage(float value)
 * {
 *     public int AsInt {
 *         get: ...
 *         set: ...
 *     }
 * }
 * 
 * // ----------
 * 
 * Percentage p = 100F
 * int i = p.AsInt
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface FloatConvertible
{
	public String methodName() default "apply";
}
