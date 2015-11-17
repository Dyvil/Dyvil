package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.util.Immutable;

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
	String VALUE_DEFAULT = "Invalid invocation of mutating method {method} on immutable type {type}";
	
	/**
	 * Returns the error to be reported by the compiler when a mutating method
	 * is called on an immutable callee.
	 * <p>
	 * The compiler will automatically replace special tokens in the returned
	 * string. These tokens include:
	 * <ul>
	 * <li>{@code method} - The name of the method
	 * <li>{@code type} - The type of the callee
	 * </ul>
	 * 
	 * @return the error to be reported by the compiler
	 */
	String value() default VALUE_DEFAULT;
}
