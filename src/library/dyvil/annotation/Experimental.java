package dyvil.annotation;

import dyvil.util.MarkerLevel;

public @interface Experimental
{
	enum Stage
	{
		DANGEROUS, UNSTABLE, UNRECOMMENDED, BETA, ALPHA, PRERELEASE;
	}
	
	public String value() default "";
	
	public Stage stage() default Stage.UNRECOMMENDED;
	
	public MarkerLevel level() default MarkerLevel.WARNING;
}
