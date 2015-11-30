package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a pure function. A pure function is a function that does not have any side effects. This means the result will
 * always be the same for the same parameters at any given time.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface pure
{
}
