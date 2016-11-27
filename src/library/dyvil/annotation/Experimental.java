package dyvil.annotation;

import dyvil.annotation.internal.ClassParameters;
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
	
	String value() default "The {member.kind} '{member.name}' is an experimental feature";

	String description() default "";
	
	Stage stage() default Stage.UNRECOMMENDED;
	
	MarkerLevel level() default MarkerLevel.WARNING;
}
