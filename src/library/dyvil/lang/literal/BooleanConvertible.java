package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a boolean literal ({@code true}
 * or {@code false}). This will cause the compiler to insert a call to a method
 * of that class with the signature {@code static X apply(boolean)}. Note that
 * such a method is automatically inserted by the <i>Dyvil Compiler</i> for any
 * {@code case class} that takes a single {@code boolean} parameter, as shown in
 * the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}BooleanConvertible
 * case class BooleanOption(boolean b)
 * 
 * // ----------
 * 
 * BooleanOption option1 = true
 * BooleanOption option2 = false
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface BooleanConvertible
{
}
