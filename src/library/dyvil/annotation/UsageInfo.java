package dyvil.annotation;

import dyvil.annotation._internal.ClassParameters;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@ClassParameters(names = { "value", "description", "level" })
public @interface UsageInfo
{
	String value();

	String description() default "";

	MarkerLevel level() default MarkerLevel.INFO;
}
