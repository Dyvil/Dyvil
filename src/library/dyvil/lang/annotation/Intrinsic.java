package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Opcodes;

/**
 * An annotation for value class methods using JVM instructions instead of an
 * actual method invocation. Instead of a {@code INVOKE} instruction, the
 * compiler inserts the opcodes specified by {@link #value()}.
 * <p>
 * The @Intrinsic annotation is also designed to work with if or while statements
 * and conditional jumps. If one uses an opcode such as GOTO or IFEQ, the
 * compiler automatically assigns the Label of the else block to that statement.
 * That means that these opcodes have to be inverted in order for the if
 * statement to work properly. For example, the integer equality comparison ==
 * uses the {@link Opcodes#IF_ICMPNE IF_ICMPNE} opcode for the jump, since the
 * JVM is supposed to jump to the else block if the two integer are
 * <i>unequal</i> (NE).
 * 
 * @author Clashsoft
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Intrinsic
{
	public int[] value();
}
