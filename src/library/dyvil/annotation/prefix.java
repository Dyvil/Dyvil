package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>prefix</b> methods. When calling the prefix method
 * {@code m(A a)} on object {@code a} of type {@code A}, {@code a} will be moved
 * from the position of the first argument to the position of the 'instance'
 * (i.e., in front of the dot) by the compiler.
 * <p>
 * <code>
 * public prefix void m() = ...<br>
 * A a = new A();<br>
 * m(a)
 * </code>
 * <p>
 * will be translated to
 * <p>
 * <code>
 * public static @prefix void m() { ... }<br>
 * A a = new A();<br>
 * B b = new B();<br>
 * a.m()
 * </code>
 * <p>
 *
 * @see Modifiers#PREFIX
 * @author Clashsoft
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface prefix
{
}
