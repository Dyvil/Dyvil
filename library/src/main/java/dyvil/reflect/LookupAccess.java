package dyvil.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * @since v0.47.0
 */
public class LookupAccess
{
	public static final MethodHandles.Lookup LOOKUP;

	static
	{
		MethodHandles.Lookup lookup;
		try
		{
			Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);
			lookup = (MethodHandles.Lookup) field.get(null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			lookup = null;
		}
		LOOKUP = lookup;
	}
}
