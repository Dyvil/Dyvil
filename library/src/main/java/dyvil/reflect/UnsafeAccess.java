package dyvil.reflect;

import java.lang.reflect.Field;

/**
 * @since v0.47.0
 */
public class UnsafeAccess
{
	public static final sun.misc.Unsafe UNSAFE;

	static
	{
		try
		{
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			UNSAFE = (sun.misc.Unsafe) field.get(null);
		}
		catch (Exception ex)
		{
			throw new Error("Cannot find Unsafe.theUnsafe", ex);
		}
	}
}
