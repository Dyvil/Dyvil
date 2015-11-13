package dyvil.annotation._internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>sealed</b> classes. This is used to mark that a class can
 * only be extended from inside the current library / project.
 * 
 * @see Modifiers#SEALED
 * @author Clashsoft
 * @version 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface sealed
{
}
