package dyvil.tools.compiler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class IOUtil
{
	public static File	javaRTJar;
	public static File	dyvilRTJar;
	
	static
	{
		String s = System.getProperty("sun.boot.class.path");
		int index = s.indexOf("rt.jar");
		if (index != -1)
		{
			int index1 = s.lastIndexOf(':', index);
			int index2 = s.indexOf(':', index + 1);
			String s1 = s.substring(index1 + 1, index2);
			javaRTJar = new File(s1);
		}
		
		// TODO Actually use the installed Dyvil Runtime Library
		dyvilRTJar = new File("bin");
	}
	
	public static Object getChildren(File parent, String child)
	{
		if (parent.isDirectory())
		{
			return new File(parent, child);
		}
		else if (parent.getPath().endsWith(".jar"))
		{
			try (JarFile jarFile = new JarFile(parent, false, ZipFile.OPEN_READ))
			{
				return jarFile.getJarEntry(child);
			}
			catch (IOException ex)
			{}
		}
		return null;
	}
	
	public static InputStream getInputStream(File parent, String child)
	{
		if (parent.isDirectory())
		{
			try
			{
				return new FileInputStream(new File(parent, child));
			}
			catch (IOException ex)
			{
				return null;
			}
		}
		else if (parent.getPath().endsWith(".jar"))
		{
			try (JarFile jarFile = new JarFile(parent, false, ZipFile.OPEN_READ))
			{
				JarEntry entry = jarFile.getJarEntry(child);
				return jarFile.getInputStream(entry);
			}
			catch (IOException ex)
			{
				return null;
			}
		}
		return null;
	}
}
