package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for value class methods using JVM instructions instead of an
 * actual method invocation.
 * 
 * @author Clashsoft
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Bytecode
{
	/**
	 * Returns the opcode of this bytecode instruction.
	 * 
	 * @return the opcode
	 */
	public int value() default -1;
	
	/**
	 * Returns all opcodes of this bytecode instruction.
	 * 
	 * @return the opcodes
	 */
	public int[] opcodes() default {};
}
