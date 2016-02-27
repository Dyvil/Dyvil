package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a integer literal. This will cause the compiler to insert a call to a
 * method of that class with the signature {@code static X apply(int)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a single {@code int} parameter, as shown
 * in the below example.
 * <p>
 * Example:
 * <p>
 * <pre>
 * {@literal @}IntConvertible
 * case class ID(int value)
 * {
 *     public void print() = System out println value
 * }
 *
 * // ----------
 *
 * ID id = 15
 * id print
 * </pre>
 *
 * @author Clashsoft
 */
public @interface IntConvertible
{
	String methodName() default "apply";
}
