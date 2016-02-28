package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a String literal. This will cause the compiler to insert a call to a
 * method of that class with the signature {@code static X apply(String)}. Note that such a method is automatically
 * inserted by the <i>Dyvil Compiler</i> for any {@code case class} that takes a single {@code String} parameter, as
 * shown in the below example.
 * <p>
 * Example:
 * <p>
 * <pre>
 * {@literal @}StringConvertible
 * case class Name(String value)
 * {
 *     public String Qualified {
 *         get: ...
 *         set: ...
 *     }
 * }
 *
 * // ----------
 *
 * Name name = "Dyvil"
 * String s = name.Qualified
 * </pre>
 *
 * @author Clashsoft
 */
public @interface StringConvertible
{
	String methodName() default "apply";
}
