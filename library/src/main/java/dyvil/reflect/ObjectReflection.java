package dyvil.reflect;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ObjectReflection
{
	public static String newUnsafeString(char[] values)
	{
		return ReflectUtils.JAVA_LANG_ACCESS.newStringUnsafe(values);
	}

	public static <T> T createInstance(String className)
	{
		try
		{
			Class c = Class.forName(className);
			return (T) c.newInstance();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static <T> T createInstance(@NonNull Class<T> c)
	{
		try
		{
			return c.newInstance();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@Nullable
	@DyvilModifiers(Modifiers.INFIX)
	public static <T> T createInstance(@NonNull Class<T> c, @NonNull Object... parameters)
	{
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++)
		{
			if (parameters[i] != null)
			{
				parameterTypes[i] = parameters[i].getClass();
			}
		}

		return createInstance(c, parameterTypes, parameters);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static <T> T createInstance(Class<T> c, Class @NonNull [] parameterTypes, Object... parameters)
	{
		try
		{
			Constructor<T> constructor = c.getConstructor(parameterTypes);
			return constructor.newInstance(parameters);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static <T> T allocateInstance(Class<T> c)
	{
		try
		{
			return (T) ReflectUtils.UNSAFE.allocateInstance(c);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static <T> void copyFields(@NonNull T from, T to)
	{
		try
		{
			Class<?> c = from.getClass();
			do
			{
				for (Field f : c.getDeclaredFields())
				{
					if ((f.getModifiers() & Modifiers.STATIC) == 0)
					{
						f.setAccessible(true);
						f.set(to, f.get(from));
					}
				}

				c = c.getSuperclass();
			}
			while (c != null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
