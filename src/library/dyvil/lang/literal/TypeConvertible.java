package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a Type Literal. This will cause
 * the compiler to insert a call to a method of that class with the signature
 * {@code static X apply(Type[Y])}, where {@code Y} is the type of the type
 * literal. Note that such a method is automatically inserted by the <i>Dyvil
 * Compiler</i> for any {@code case class} that takes a single {@code Type[Y]}
 * parameter, as shown in the below example.
 * <p>
 * Example:
 * <p>
 * <pre>
 * {@literal @}TypeConvertible
 * case class TypeRef(Type[_] value)
 *
 * // ----------
 *
 * TypeRef t = type[String]
 * </pre>
 *
 * @author Clashsoft
 */
public @interface TypeConvertible
{
	String methodName() default "apply";
}
