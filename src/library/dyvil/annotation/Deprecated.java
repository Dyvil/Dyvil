package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
import dyvil.annotation.internal.NonNull;
import dyvil.util.MarkerLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@ClassParameters(names = { "value", "description", "since", "reasons", "replacements", "level" })
public @interface Deprecated
{
	enum Reason
	{
		UNSPECIFIED, DANGEROUS, CONDEMNED, SUPERSEDED, UNIMPLEMENTED
	}

	@NonNull String value() default "The {member.kind} '{member.name}' is deprecated";

	@NonNull String description() default "";

	@NonNull String since() default "";

	@NonNull Reason @NonNull [] reasons() default { Reason.UNSPECIFIED };

	@NonNull String @NonNull [] replacements() default {};

	@NonNull MarkerLevel level() default MarkerLevel.WARNING;
}
