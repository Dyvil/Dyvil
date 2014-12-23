package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for value class methods using JVM instructions instead of an
 * actual method invocation. A method call with a Bytecode annotation will be
 * compiled as follows:
 * <ul>
 * <li>process {@code prefixOpcodes} (or, if empty, {@code prefixOpcode})
 * <b>instructions</b>
 * <li>push the <b>instance</b> (i.e., the first operand) on the stack
 * <li>process {@code infixOpcodes} (or, if empty, {@code infixOpcode})
 * <b>instructions</b>
 * <li>push all <b>arguments</b> (i.e., the other operands) on the stack
 * <li>process {@code opcodes} (or, if empty, {@code opcode})
 * <b>instructions</b>
 * 
 * @author Clashsoft
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Bytecode
{
	/**
	 * Returns the opcode of this bytecode instruction that is directly followed
	 * by the instance (i.e, the first operand) of the invocation.
	 * 
	 * @return the opcode between the operands
	 */
	public int prefixOpcode() default -1;
	
	/**
	 * Returns the opcodes of this bytecode instruction that is directly
	 * followed by the instance (i.e., the first operand) of the invocation.
	 * 
	 * @return the opcodes between the operands
	 */
	public int[] prefixOpcodes() default {};
	
	/**
	 * Returns the opcode of this bytecode instruction that directly follows the
	 * instance (i.e, the first operand) of the invocation.
	 * 
	 * @return the opcode between the operands
	 */
	public int infixOpcode() default -1;
	
	/**
	 * Returns the opcodes of this bytecode instruction that directly follow the
	 * instance (i.e., the first operand) of the invocation.
	 * 
	 * @return the opcodes between the operands
	 */
	public int[] infixOpcodes() default {};
	
	/**
	 * Returns the opcode of this bytecode instruction that directly follows the
	 * arguments (i.e, the second operand) of the invocation.
	 * 
	 * @return the opcode after the operands
	 */
	public int postfixOpcode() default -1;
	
	/**
	 * Returns the opcodes of this bytecode instruction that directly follow the
	 * arguments (i.e, the second operand) of the invocation.
	 * 
	 * @return the opcodes after the operands
	 */
	public int[] postfixOpcodes() default {};
}
