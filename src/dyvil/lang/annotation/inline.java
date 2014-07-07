package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods that can be inlined by the compiler.
 *
 * @author Clashsoft
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface inline
{}
