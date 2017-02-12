package dyvil.annotation;

import dyvil.annotation.internal.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that a Class or Method Type Parameter is <b>reified</b>. If a Method Parameter is annotated with this
 * annotation, it receives an additional mandated parameter that holds a {@link dyvil.reflect.types.Type Type} or
 * {@link Class} instance representing the Type Argument. The {@link #value()} annotation parameter defines whether the
 * full generic type or only the erasure is needed.<p/>If the full generic type is reified, it is retrievable and usable
 * with the {@code type<T>} operator. In both cases, the erasure is available using the {@code class<T>} operator.<p/>
 * When using the {@link Reified.Type#ANY_CLASS} enum constant, primitive types are passed to the {@link Class}
 * parameter as their primitive type {@link Class} instance. The {@link Reified.Type#OBJECT_CLASS} ensures that the
 * passed {@link Class} instance refers to a reference type, i.e. a wrapper type for primitives.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface Reified
{
	enum Type
	{
		ANY_CLASS, OBJECT_CLASS, TYPE;
	}

	/**
	 * @return The reified parameter type
	 *
	 * @see Reified.Type
	 */
	@NonNull Type value() default Type.ANY_CLASS;
}
