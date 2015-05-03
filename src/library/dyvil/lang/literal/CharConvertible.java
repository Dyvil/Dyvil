package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a char literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(char)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a
 * single {@code char} parameter, as shown in the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}CharConvertible
 * case class Key(char c)
 * {
 *     public boolean isPressed() = ...
 * }
 * 
 * // ----------
 * 
 * Key key = 'A'
 * if (key.isPressed) { ... }
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface CharConvertible
{
}
