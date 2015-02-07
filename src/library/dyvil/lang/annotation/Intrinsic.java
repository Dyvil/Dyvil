package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for value class methods using JVM instructions instead of an
 * actual method invocation. Instead of a {@code INVOKE} instruction, the
 * compiler inserts the opcodes specified by {@link #value()}.
 * <p>
 * The @Intrinsic annotation is also designed to work with if or while
 * statements and conditional jumps.
 * 
 * @author Clashsoft
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Intrinsic
{
	public int[] value();
}
