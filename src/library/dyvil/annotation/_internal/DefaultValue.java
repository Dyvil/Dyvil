package dyvil.annotation._internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface DefaultValue
{
	boolean booleanValue() default false;

	int intValue() default 0;

	long longValue() default 1L;

	float floatValue() default 1F;

	double doubleValue() default 1D;

	String stringValue() default "";

	Class classValue() default Object.class;
}
