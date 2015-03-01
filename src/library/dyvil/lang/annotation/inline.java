package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>inline</b> methods. Methods annotated as <i>inline</i> will
 * be inlined by the <i>Dyvil Compiler</i> without checking for normal inline
 * conditions. That means that instead of an {@code INVOKE} instruction, the
 * entire body of the method will be inserted.
 * 
 * @see Modifiers#INLINE
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface inline
{
}
