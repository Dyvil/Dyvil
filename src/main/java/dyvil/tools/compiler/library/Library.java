package dyvil.tools.compiler.library;

import java.io.File;
import java.io.InputStream;

import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.ClassFormat;

public abstract class Library
{
	public static Library	dyvilLibrary	= load(ClassFormat.dyvilRTJar);
	public static Library	javaLibrary		= load(ClassFormat.javaRTJar);
	
	public File				file;
	
	protected Library(File file)
	{
		this.file = file;
	}
	
	public static Library load(File file)
	{
		if (file.isDirectory())
		{
			return new FileLibrary(file);
		}
		else if (file.getPath().endsWith(".jar"))
		{
			return new JarLibrary(file);
		}
		return null;
	}
	
	public abstract void loadLibrary();
	
	public abstract Package resolvePackage(String name);
	
	public abstract InputStream getInputStream(String fileName);
	
	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}
}
