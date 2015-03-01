package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>lazy</b> fields. If a field is marked with this modifier,
 * it will be evaluated every time it is demanded and is thus not saved in the
 * memory. This behavior can be compared with a method without parameters.
 *
 * @see Modifiers#LAZY
 * @author Clashsoft
 * @version 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.LOCAL_VARIABLE })
public @interface lazy
{
}
