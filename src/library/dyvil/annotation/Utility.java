package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class marked as a <b>Utility</b> class provides utility methods for any or
 * a given type. A utility should either be an {@code interface} or a {final
 * class} without a public constructor.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Utility
{
	/**
	 * The classes of objects on which this {@linkplain Utility} class operates.
	 * 
	 * @return the classes
	 */
	public Class[] value() default {};
}
