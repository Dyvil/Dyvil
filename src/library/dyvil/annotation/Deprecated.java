package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
import dyvil.annotation.internal.NonNull;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@ClassParameters(names = { "value", "description", "since", "forRemoval", "reasons", "replacements", "level" })
public @interface Deprecated
{
	enum Reason
	{
		DANGEROUS, CONDEMNED, SUPERSEDED, UNIMPLEMENTED
	}

	/**
	 * @return the compiler diagnostic message text
	 */
	@NonNull String value() default "The {member.kind} '{member.name}' is deprecated";

	/**
	 * @return a description on why the member was deprecated
	 */
	@NonNull String description() default "";

	/**
	 * @return since when the member is deprecated
	 */
	@NonNull String since() default "";

	/**
	 * @return when the member is scheduled for removal
	 */
	@NonNull String forRemoval() default "";

	/**
	 * @return a list of reasons why the member is deprecated
	 */
	@NonNull Reason @NonNull [] reasons() default {};

	/**
	 * @return a list of replacement members that should be used instead
	 */
	@NonNull String @NonNull [] replacements() default {};

	/**
	 * @return the marker level of the compiler diagnostic message
	 */
	@NonNull MarkerLevel level() default MarkerLevel.WARNING;
}
