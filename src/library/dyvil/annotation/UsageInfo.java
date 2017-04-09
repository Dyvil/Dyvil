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
	/**
	 * @return the compiler diagnostic message text
	 */
	@NonNull String value();

	/**
	 * @return an extended description about the usage of the member
	 */
	@NonNull String description() default "";

	/**
	 * @return the marker level of the compiler diagnostic message
	 */
	@NonNull MarkerLevel level() default MarkerLevel.INFO;
}
