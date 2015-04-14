package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a mutating method, i.e. a method that mutates the members of a mutable
 * type. If a method annotated as {@code mutating} is called on any instance of
 * the {@link Immutable} interface, compiler should report the error specified
 * by {@link #error()}.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface mutating
{
	/**
	 * Returns the error to be reported by the compiler. The strings {@code method}}
	 * and {@code type}} are automatically replaced with the method name and
	 * the type name of the callee, respectively.
	 * 
	 * @return the error to be reported by the compiler
	 */
	public String error() default "Invalid invocation of mutating method {method} on immutable type {type}";
}
