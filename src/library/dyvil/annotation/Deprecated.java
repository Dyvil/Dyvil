package dyvil.annotation;

import dyvil.util.MarkerLevel;

public @interface Deprecated
{
	public enum Reason
	{
		UNSPECIFIED, DANGEROUS, CONDEMNED, SUPERSEDED, UNIMPLEMENTED;
	}
	
	public String value() default "";
	
	public String marker() default "The {member} is deprecated";
	
	public String since() default "";
	
	public Reason[]reasons() default { Reason.UNSPECIFIED };
	
	public String[]replacements() default {};
	
	public MarkerLevel level() default MarkerLevel.WARNING;
}
