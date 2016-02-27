package dyvil.annotation;

import dyvilx.lang.model.type.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that a Class or Method Type Parameter is <b>reified</b>. If a Method Parameter is annotated with this
 * annotation, it receives an additional mandated parameter that holds a {@link Type} or {@link Class} instance
 * representing the Type Argument. For classes, an additional field is generated that holds this instance. The {@link
 * #erasure()} annotation parameter defines whether the full type or only the erasure is needed.<p/> If the full generic
 * type is reified, it is retrievable and usable using the {@code type(T)} operator. In both cases, the erasure is
 * available using the {@code class(T)} operator.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface Reified
{
	/**
	 * Marks that only the erasure needs to be available at the declaration site. This improves performance in that no
	 * {@link Type Type} instances need to be created at the call site. If this flag is set, the mandated parameter will
	 * have the type {@link Class}. Otherwise, the mandated parameter type will have the type {@link Type Type}.
	 *
	 * @return true, if only the erasure {@link Class} is needed.
	 */
	boolean erasure() default false;
}
