package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a Class Literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(Class[Y])}, where {@code Y} is the type of the class literal. Note
 * that such a method is automatically inserted by the <i>Dyvil Compiler</i> for
 * any {@code case class} that takes a single {@code Class[X]} parameter, as shown in
 * the below example.
 * <p>
 * Example:
 * 
 * <pre>
 * {@literal @}ClassConvertible
 * case class Type(Class[_] value)
 * 
 * // ----------
 * 
 * Type t = class[String]
 * </pre>
 * 
 * @author Clashsoft
 */
public @interface ClassConvertible
{	
}
