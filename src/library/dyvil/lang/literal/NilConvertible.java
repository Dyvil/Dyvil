package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with the {@code nil} literal. This
 * will cause the compiler to insert a call to a method of that class with the
 * signature {@code static X apply()}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that does
 * not take any parameters, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}NilConvertible
 * case class Nothing
 * 
 * // ----------
 * 
 * Nothing nothing = nil
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface NilConvertible
{
	String methodName() default "apply";
}
