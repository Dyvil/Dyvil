package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
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
	
	String value() default "The {member.kind} '{member.name}' is deprecated";

	String description() default "";
	
	String since() default "";
	
	Reason[] reasons() default { Reason.UNSPECIFIED };
	
	String[] replacements() default {};
	
	MarkerLevel level() default MarkerLevel.WARNING;
}
