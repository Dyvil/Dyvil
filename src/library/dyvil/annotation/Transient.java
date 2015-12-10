package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Substitute annotation for the {@code transient} keyword in Java. The <i>Dyvil
 * Compiler</i> directly converts this annotation to the {@code ACC_TRANSIENT}
 * flag.
 *
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Transient
{}
