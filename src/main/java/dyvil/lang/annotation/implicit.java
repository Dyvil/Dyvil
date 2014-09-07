package dyvil.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for implicit methods. If a method is annotated as <i>implicit</i>,
 * the first argument is implicit. That means that when calling the static
 * method {@code m(A a, B b)} on object {@code a} of type {@code A}, {@code a}
 * will be moved from the position before the dot to the position of the first
 * argument by the compiler.
 * <p>
 * <code>
 * public static @implicit void m(A a, B b)
 * {
 * }<br>
 * A a = new A();<br>
 * B b = new B();<br>
 * a.m(b);
 * </code>
 * <p>
 * will be translated to
 * <p>
 * <code>
 * public static void m(A a, B b)
 * {
 * }<br>
 * A a = new A();<br>
 * B b = new B();<br>
 * m(a, b);
 * </code>
 * <p>
 *
 * @author Clashsoft
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface implicit
{}
