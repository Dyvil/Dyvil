package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface bytecode
{
	/**
	 * The opcode of this bytecode instruction.
	 * @return the opcode
	 */
	public int value();
}
