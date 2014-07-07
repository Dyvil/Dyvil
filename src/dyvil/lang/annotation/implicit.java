package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for implicit methods.
 *
 * @author Clashsoft
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface implicit
{
	public Class[] classes() default {};
}
