package dyvil.annotation._internal;

import dyvil.reflect.Modifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for <b>internal</b> classes and members. This is used to mark that
 * a class, method or field is only visible from inside the current library /
 * project.
 *
 * @author Clashsoft
 * @version 1.0
 * @see Modifiers#INTERNAL
 */
@Retention(RetentionPolicy.CLASS)
@Target( { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface internal
{}
