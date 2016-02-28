package dyvil.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodReflection
{
	public static final MethodHandles.Lookup LOOKUP;
	
	static
	{
		Lookup lookup;
		try
		{
			Field f = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			f.setAccessible(true);
			lookup = (Lookup) f.get(null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			lookup = null;
		}
		LOOKUP = lookup;
	}
	
	// Methods
	
	/**
	 * Returns the method of the given {@link Class} {@code class} specified by the given {@code object}. <ul> <li>If
	 * {@code object} is a {@link Method} instance, it returns the object. <li>If {@code object} is an integer, it
	 * returns the {@link Method} of the given {@link Class} {@code class} with the id {@code object}. <li>If {@code
	 * object} is an Object[] of length 2, it <ul> <li>Returns the method with the name {@code object[0]} if {@code
	 * object[0]} is a String <li>Returns the method with the name of any {@code object[0]} if {@code object[0]} is a
	 * String[] </ul> </ul>
	 *
	 * @param clazz
	 * 		the clazz
	 * @param object
	 * 		the object
	 *
	 * @return the method
	 */
	public static Method getMethod(Class clazz, Object object)
	{
		if (object == null)
		{
			throw new NullPointerException("Cannot get null method!");
		}
		Class c = object.getClass();
		if (c == Method.class)
		{
			return (Method) object;
		}
		else if (c == int.class)
		{
			return getMethod(clazz, (int) object);
		}
		else if (c == Object[].class)
		{
			Object[] aobject = (Object[]) object;
			if (aobject.length == 2)
			{
				if (aobject[0] instanceof String)
				{
					return getMethod(clazz, (String) aobject[0], (Class[]) aobject[1]);
				}
				else if (aobject[0] instanceof String[])
				{
					return getMethod(clazz, (String[]) aobject[0], (Class[]) aobject[1]);
				}
			}
		}
		System.err.println("Unable to get method specified with " + object);
		return null;
	}
	
	/**
	 * Returns the {@link Method} of the given {@link Class} {@code clazz} with the given name {@code methodName} and
	 * the given parameter types {@code parameterTypes}.
	 *
	 * @param clazz
	 * 		the clazz
	 * @param methodName
	 * 		the method name
	 * @param parameterTypes
	 * 		the parameter types
	 *
	 * @return the method
	 */
	public static Method getMethod(Class clazz, String methodName, Class[] parameterTypes)
	{
		try
		{
			Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
			if (m != null)
			{
				return m;
			}
		}
		catch (NoSuchMethodException ex)
		{
		}
		catch (SecurityException ex)
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
	 * 		the clazz
	 * @param methodNames
	 * 		the possible method names
	 * @param parameterTypes
	 * 		the parameter types
	 *
	 * @return the method
	 */
	public static Method getMethod(Class clazz, String[] methodNames, Class[] parameterTypes)
	{
		for (String methodName : methodNames)
		{
			Method m = getMethod(clazz, methodName, parameterTypes);
			if (m != null)
			{
				return m;
			}
		}
		System.err.println(new NoSuchMethodException(
				"Method not found! (Class: " + clazz + "; Expected field names: " + Arrays.toString(methodNames)));
		return null;
	}
	
	/**
	 * Returns the {@link Method} of the given {@link Class} {@code clazz} with the given method ID {@code methodID}.
	 *
	 * @param clazz
	 * 		the clazz
	 * @param methodID
	 * 		the method ID
	 *
	 * @return the method
	 */
	public static Method getMethod(Class clazz, int methodID)
	{
		return clazz.getDeclaredMethods()[methodID];
	}
	
	// Method invocation
	
	// Reference
	
	public static <T, R> R invokeStatic(Class<? super T> clazz, Object[] args, Object method)
	{
		return invoke(clazz, null, args, method);
	}
	
	public static <T, R> R invoke(T instance, Object[] args, Object method)
	{
		return invoke((Class<T>) instance.getClass(), instance, args, method);
	}
	
	public static <T, R> R invoke(Class<? super T> clazz, T instance, Object[] args, Object method)
	{
		Method m = getMethod(clazz, method);
		return invoke(m, instance, args);
	}
	
	// Method ID
	
	public static <T, R> R invokeStatic(Class<? super T> clazz, Object[] args, int methodID)
	{
		return invoke(clazz, null, args, methodID);
	}
	
	public static <T, R> R invoke(T instance, Object[] args, int methodID)
	{
		return invoke((Class<T>) instance.getClass(), instance, args, methodID);
	}
	
	public static <T, R> R invoke(Class<? super T> clazz, T instance, Object[] args, int methodID)
	{
		Method m = getMethod(clazz, methodID);
		return invoke(m, instance, args);
	}
	
	/**
	 * Directly invokes the given {@link Method} {@code method} on the given {@link Object} {@code instance} with the
	 * given arguments {@code args} and returns the result.
	 *
	 * @param method
	 * 		the method to invoke
	 * @param instance
	 * 		the instance
	 * @param args
	 * 		the arguments
	 *
	 * @return the result
	 */
	public static <R> R invoke(Method method, Object instance, Object[] args)
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
