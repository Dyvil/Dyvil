package dyvil.reflect;

import dyvil.annotation.internal.NonNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @deprecated since v0.47.0
 */
@Deprecated
public final class ReflectUtils
{
	/**
	 * @deprecated since v0.47.0; use {@link UnsafeAccess#UNSAFE}
	 */
	@Deprecated
	public static final sun.misc.Unsafe UNSAFE = UnsafeAccess.UNSAFE;

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

	/**
	 * @deprecated since v0.47.0
	 */
	@Deprecated
	@NonNull
	public static File getFileLocation(@NonNull Class<?> klass) throws ClassNotFoundException
	{
		final String classLocation = '/' + klass.getName().replace('.', '/') + ".class";
		final URL url = klass.getResource(classLocation);

		if (url == null)
		{
			throw new ClassNotFoundException("Location not found: " + classLocation);
		}

		final String path = url.toString().replace(File.separatorChar, '/');
		int index = path.lastIndexOf(classLocation);

		if (index < 0)
		{
			throw new ClassNotFoundException("Invalid Path: " + path);
		}

		int startIndex = 0;
		if (path.charAt(index - 1) == '!')
		{
			index--;
			startIndex = 4; // strip leading 'jar:'
		}
		else
		{
			index++;
		}

		final String newPath = path.substring(startIndex, index);
		try
		{
			return new File(new URI(newPath));
		}
		catch (URISyntaxException ex)
		{
			throw new ClassNotFoundException("Invalid URI: " + newPath, ex);
		}
	}
}
