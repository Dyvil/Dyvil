package dyvil.annotation.analysis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Contract
{
	String value();
}
