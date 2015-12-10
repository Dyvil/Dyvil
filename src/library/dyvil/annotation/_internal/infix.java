package dyvil.annotation._internal;

import dyvil.reflect.Modifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for <b>infix</b> methods. When calling the infix method
 * {@code m(A a, B b)} on object {@code a} of type {@code A}, {@code a} will be
 * moved from the 'instance' position (i.e., in front of the dot) to the
 * position of the first argument by the compiler.
 * <p>
 * <code>
 * public infix void m(A a, B b) = ...<br>
 * A a = new A();<br>
 * B b = new B();<br>
 * a.m(b);
 * </code>
 * <p>
 * will be translated to
 * <p>
 * <code>
 * public static @infix void m(A a, B b) { ... }<br>
 * A a = new A();<br>
 * B b = new B();<br>
 * m(a, b);
 * </code>
 * <p>
 *
 * @author Clashsoft
 * @version 1.0
 * @see Modifiers#INFIX
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface infix
{}
