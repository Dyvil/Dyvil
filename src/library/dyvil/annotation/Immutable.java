package dyvil.annotation;

import dyvil.collection.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks types as <i>immutable</i>. This interface is primarily designed to be used in conjunction with
 * the {@link Mutating} annotation, which allows compile-time checking mutating access to immutable types, especially
 * {@link Collection collections}. The Immutable Annotation can be used both on ordinary classes or interfaces and on
 * types (i.e. references to classes or interfaces). In both cases, the effects of using it will be the same.
 *
 * @author Clashsoft
 * @see Mutating
 * @see ImmutableCollection
 * @see ImmutableList
 * @see ImmutableMap
 * @see ImmutableMatrix
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.TYPE_USE })
public @interface Immutable
{}
