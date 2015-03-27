package dyvil.lang.literal;

/**
 * Marks a class that can be instantiated with a String literal. The process of
 * doing so will cause the compiler to insert a call to a constructor of that
 * class that takes the String literal as a single argument.
 * <p>
 * Example:
 * <pre>
 * public class Name implements StringConvertible
 * {
 *     private String name;
 *     
 *     public Name new(String value) { name = value }
 *     
 *     public String getFormattedName() = ...
 * }
 * 
 * ----------
 * 
 * Name name = "Dyvil"
 * String s = name getFormattedName
 * </pre>
 * 
 * @author Clashsoft
 */
public interface StringConvertible
{
}
