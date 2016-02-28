package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Substitute annotation for the {@code native} keyword in Java. The <i>Dyvil Compiler</i> directly converts this
 * annotation to the {@code ACC_NATIVE} flag.
 *
 * @author Clashsoft
 * @version 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target( { ElementType.FIELD, ElementType.METHOD })
public @interface Native
{}
