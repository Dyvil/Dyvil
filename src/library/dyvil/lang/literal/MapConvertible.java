package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a map literal. This will cause the compiler to insert a call to a method
 * of that class with the signature {@code static X apply([K], [V])}, where {@code K} is the key type and {@code V} is
 * the value type of the map.
 * <p>
 * Example:
 * <p>
 * <pre>
 * {@literal @}MapConvertible
 * case class ArrayMap[K, V]([K] keys, [V] values)
 *
 * // ----------
 *
 * ArrayMap[int, String] map = [ 1 : "a", 2 : "b", 3 : "c" ]
 * </pre>
 *
 * @author Clashsoft
 */
public @interface MapConvertible
{
	String methodName() default "apply";
}
