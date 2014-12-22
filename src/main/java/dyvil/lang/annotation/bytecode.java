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
	 * Returns the opcode of this bytecode instruction that directly follows the
	 * instance (i.e, the first operand) of the invocation.
	 * 
	 * @return the opcode between the operands
	 */
	public int infixOpcode() default -1;
	
	/**
	 * Returns the opcode of this bytecode instruction that directly follows the
	 * arguments (i.e, the second operand) of the invocation.
	 * 
	 * @return the opcode after the operands
	 */
	public int opcode() default -1;
	
	/**
	 * Returns the opcodes of this bytecode instruction that directly follow the
	 * instance (i.e., the first operand) of the invocation.
	 * 
	 * @return the opcodes between the operands
	 */
	public int[] infixOpcodes() default {};
	
	/**
	 * Returns the opcodes of this bytecode instruction that directly follow the
	 * arguments (i.e, the second operand) of the invocation.
	 * 
	 * @return the opcodes after the operands
	 */
	public int[] opcodes() default {};
}
