package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a Format String. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(String, Object...)}.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}FormatStringConvertible
 * case class Format(String format, Object... values)
 * 
 * // ----------
 * 
 * String world = "World"
 * Format f = @"Hello {world}"
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface FormatStringConvertible
{
	public String methodName() default "apply";
}
