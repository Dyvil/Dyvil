package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
import dyvil.annotation.internal.NonNull;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@ClassParameters(names = { "value", "description", "stage", "level" })
public @interface Experimental
{
	enum Stage
	{
		DANGEROUS, UNSTABLE, UNRECOMMENDED, BETA, ALPHA, PRERELEASE
	}

	/**
	 * @return the compiler diagnostic message text
	 */
	@NonNull String value() default "The {member.kind} '{member.name}' is an experimental feature";

	/**
	 * @return a description on the experimental status of the member
	 */
	@NonNull String description() default "";

	/**
	 * @return the current experimental stage of the member
	 */
	@NonNull Stage stage() default Stage.UNRECOMMENDED;

	/**
	 * @return the marker level of the compiler diagnostic message
	 */
	@NonNull MarkerLevel level() default MarkerLevel.WARNING;
}
