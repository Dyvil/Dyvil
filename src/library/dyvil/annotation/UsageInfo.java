package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@ClassParameters(names = { "value", "description", "level" })
public @interface UsageInfo
{
	String value();

	String description() default "";

	MarkerLevel level() default MarkerLevel.INFO;
}
