package dyvil.reflect;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import java.lang.reflect.Field;

public final class ReflectUtils
{
	protected static final JavaLangAccess JAVA_LANG_ACCESS = SharedSecrets.getJavaLangAccess();
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
	
	private ReflectUtils()
	{
		throw new Error("No instances");
	}
	
	// Classes
	
	@Deprecated
	public static boolean checkClass(String name)
	{
		try
		{
			Class.forName(name, false, ClassLoader.getSystemClassLoader());
			return true;
		}
		catch (ClassNotFoundException ex)
		{
			return false;
		}
	}
	
	@Deprecated
	public static Class getClass(String name)
	{
		try
		{
			return Class.forName(name, false, ClassLoader.getSystemClassLoader());
		}
		catch (ClassNotFoundException ex)
		{
			return null;
		}
	}
}
