package dyvil.annotation;

import dyvil.util.MarkerLevel;

public @interface Deprecated
{
	enum Reason
	{
		UNSPECIFIED, DANGEROUS, CONDEMNED, SUPERSEDED, UNIMPLEMENTED
	}
	
	String value() default "";
	
	String marker() default "The {member} is deprecated";
	
	String since() default "";
	
	Reason[]reasons() default { Reason.UNSPECIFIED };
	
	String[]replacements() default {};
	
	MarkerLevel level() default MarkerLevel.WARNING;
}
