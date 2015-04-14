package dyvil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dyvil.reflect.Modifiers;

/**
 * Annotation for <b>lazy</b> fields, variables and parameters. The behavior of
 * the {@code lazy} modifier depends on the type of the field it is applied to,
 * <ul>
 * <li>If a <b>Field</b> (instance or static) is marked as {@code lazy}, it will
 * be evaluated the first time it is demanded and then stored in memory for
 * later use.
 * <li>If a <b>Variable</b> is marked as {@code lazy}, it will be evaluated
 * every time it is demanded and is thus not saved in the memory. This behavior
 * can be compared to a method without parameters.
 * <li>If a formal <b>Parameter</b> of a method is marked as {@code lazy}, it
 * will behave like a Call-By-Name parameter. That means that values passed for
 * this parameter will be converted to a <b>thunk</b> that gets evaluated every
 * time the parameter is used inside the body of the method instead of at
 * call-site.
 * </ul>
 *
 * @see Modifiers#LAZY
 * @author Clashsoft
 * @version 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER })
public @interface lazy
{
}
