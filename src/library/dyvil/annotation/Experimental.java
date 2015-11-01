package dyvil.annotation;

public @interface Experimental
{
	enum Stage
	{
		DANGEROUS, UNSTABLE, UNRECOMMENDED, BETA, ALPHA, PRERELEASE;
	}
	
	public String value();
	
	public Stage stage() default Stage.UNRECOMMENDED;
}
