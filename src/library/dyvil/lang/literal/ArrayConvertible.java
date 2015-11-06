package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with an array literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply([E])}, where {@code E} is the element type of the
 * array. Note that such a method is automatically inserted by the <i>Dyvil
 * Compiler</i> for any {@code case class} that takes a single {@code [E]}
 * parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}ArrayConvertible
 * case class IntVector([int] value)
 * 
 * // ----------
 * 
 * IntVector vec = [ 1, 2, 3 ]
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface ArrayConvertible
{
	public String methodName() default "apply";
}
