package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for value class methods using JVM instructions instead of an
 * actual method invocation. Instead of a {@code INVOKE} instruction, the
 * compiler inserts the opcodes specified by {@link #value()}.
 * <p>
 * The <b>Intrinsic</b> annotation is also designed to work with {@code if} or
 * {@code while} statements and conditional jumps. Jump instructions that would
 * require a Label as parameter can be part of the {@link #value() opcode array}
 * , and the <i>Dyvil Compiler</i> will automatically insert the Labels after
 * the opcode.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Intrinsic
{
	public int[] value();
}
