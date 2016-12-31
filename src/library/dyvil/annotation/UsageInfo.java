package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
import dyvil.annotation.internal.NonNull;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@ClassParameters(names = { "value", "description", "level" })
public @interface UsageInfo
{
	@NonNull String value();

	@NonNull String description() default "";

	@NonNull MarkerLevel level() default MarkerLevel.INFO;
}
