package dyvil.reflect;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @deprecated since v0.47.0
 */
@Deprecated
@SuppressWarnings( { "unused", "unchecked" })
public class MethodReflection
{
	/**
	 * @deprecated since v0.47.0; use {@link LookupAccess#LOOKUP} instead
	 */
	@Deprecated
	public static final MethodHandles.Lookup LOOKUP = LookupAccess.LOOKUP;

	// Methods

	/**
	 * Returns the {@link Method} of the given {@link Class} {@code clazz} with the given name {@code methodName} and
	 * the given parameter types {@code parameterTypes}.
	 *
	 * @param clazz
	 * 	the clazz
	 * @param methodName
	 * 	the method name
	 * @param parameterTypes
	 * 	the parameter types
	 *
	 * @return the method
	 *
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static Method getMethod(@NonNull Class<?> clazz, @NonNull String methodName,
		@NonNull Class<?> @NonNull [] parameterTypes)
	{
		try
		{
			return clazz.getDeclaredMethod(methodName, parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the {@link Method} of the given {@link Class} {@code clazz} with a name contained in {@code methodNames}
	 * and the given parameter types {@code parameterTypes}.
	 *
	 * @param clazz
	 * 	the clazz
	 * @param methodNames
	 * 	the possible method names
	 * @param parameterTypes
	 * 	the parameter types
	 *
	 * @return the method
	 *
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static Method getMethod(@NonNull Class<?> clazz, @NonNull String @NonNull [] methodNames,
		@NonNull Class<?> @NonNull [] parameterTypes)
	{
		for (String methodName : methodNames)
		{
			Method method = getMethod(clazz, methodName, parameterTypes);
			if (method != null)
			{
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Method} of the given {@link Class} {@code clazz} with the given method ID {@code methodID}.
	 *
	 * @param clazz
	 * 	the clazz
	 * @param methodID
	 * 	the method ID
	 *
	 * @return the method
	 *
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static @NonNull Method getMethod(@NonNull Class<?> clazz, int methodID)
	{
		return clazz.getDeclaredMethods()[methodID];
	}

	// Method invocation

	// Reference

	// Method ID

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	@Nullable
	public static <T, R> R invokeStatic(@NonNull Class<? super T> clazz, Object @NonNull [] args, int methodID)
	{
		return invoke(clazz, null, args, methodID);
	}

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	@Nullable
	public static <T, R> R invoke(@NonNull T instance, Object @NonNull [] args, int methodID)
	{
		return invoke((Class<T>) instance.getClass(), instance, args, methodID);
	}

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	@Nullable
	public static <T, R> R invoke(@NonNull Class<? super T> clazz, T instance, Object @NonNull [] args, int methodID)
	{
		return invoke(getMethod(clazz, methodID), instance, args);
	}

	/**
	 * Directly invokes the given {@link Method} {@code method} on the given {@link Object} {@code instance} with the
	 * given arguments {@code args} and returns the result.
	 *
	 * @param <R>
	 * 	the return type
	 * @param method
	 * 	the method to invoke
	 * @param instance
	 * 	the instance
	 * @param args
	 * 	the arguments
	 *
	 * @return the result
	 *
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	public static <R> R invoke(Method method, Object instance, Object @NonNull [] args)
	{
		try
		{
			method.setAccessible(true);
			return (R) method.invoke(instance, args);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
