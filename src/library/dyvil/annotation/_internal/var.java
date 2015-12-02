package dyvil.annotation._internal;

import dyvil.reflect.Modifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for <b>variable</b> parameters. This is used to mark that a
 * parameter is Call-By-Reference. If a parameter doesn't have this annotation
 * or modifier, it behaves like a normal formal parameter. Otherwise, it only
 * accepts references to fields or variables on call-site. When a {@code var}
 * parameter is assigned from within the method body of the method it is a
 * formal parameter of, the assignment applies for passed field or variable
 * instead of the parameter. Thus, the following code is valid:
 * <p>
 * <pre>
 * void swap(var int i, var int j)
 * {
 *     int k = j
 *     j = i
 *     i = k
 * }
 *
 * int a = 2
 * int b = 5
 * swap(a, b)
 *
 * println(a) // prints '5'
 * println(b) // prints '2'
 * </pre>
 * <p>
 * Note that for this particular example, it is advised to use the Swap Operator
 * {@code :=:} as it is designed for this operation and utilizes the stack
 * instead of local variables.
 *
 * @author Clashsoft
 * @version 1.0
 * @see Modifiers#VAR
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface var
{}
