package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a long literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(long)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a
 * single {@code long} parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}LongConvertible
 * case class ID(long value)
 * {
 *     public void print() = System out println value
 * }
 * 
 * // ----------
 * 
 * ID id = 0x186592739619239L
 * id print
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface LongConvertible
{
	String methodName() default "apply";
}
