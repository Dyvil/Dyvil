package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for inline methods. Methods annotated as <i>inline</i> will be
 * inlined by the compiler without checking for normal inline conditions. That
 * means that instead of an INVOKE instruction, the entire body of the method
 * will be inserted.
 *
 * @author Clashsoft
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface inline
{
}
