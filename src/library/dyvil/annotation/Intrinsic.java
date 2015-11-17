package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for <b>intrinsic</b> methods using JVM instructions for method
 * invocation. Instead of a {@code INVOKE} instruction, the compiler inserts the
 * opcodes specified by {@link #value()}. This annotation is primarily used for
 * methods that have a very common implementation such that simple inlining
 * performed by the compiler is not efficient enough, since it has to store all
 * parameters to the inlined call into local variables first. A typical example
 * for an intrinsic method is the
 * {@link dyvil.lang.Predef#$eq$eq(Object, Object) ==} operator.
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
	int[]value();
	
	String[] strings() default {};
}
