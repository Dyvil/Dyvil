package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>byref</b> parameters. This is used to mark that a parameter
 * is Call-By-Reference. If a parameter doesn't have this annotation or
 * modifier, it is Call-By-Value.
 * 
 * @see Modifiers#BYREF
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface byref
{
}
