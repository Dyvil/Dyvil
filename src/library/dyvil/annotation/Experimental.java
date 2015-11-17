package dyvil.annotation;

import dyvil.util.MarkerLevel;

public @interface Experimental
{
	enum Stage
	{
		DANGEROUS, UNSTABLE, UNRECOMMENDED, BETA, ALPHA, PRERELEASE
	}
	
	String value() default "";
	
	Stage stage() default Stage.UNRECOMMENDED;
	
	MarkerLevel level() default MarkerLevel.WARNING;
}
