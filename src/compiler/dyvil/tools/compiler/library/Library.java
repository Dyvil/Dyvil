package dyvil.tools.compiler.library;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.structure.ExternalPackage;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public abstract class Library
{
	public static Library		dyvilLibrary	= load(ClassFormat.dyvilRTJar);
	public static Library		javaLibrary		= load(ClassFormat.javaRTJar);
	
	public File					file;
	public Map<String, Package>	packages		= new HashMap();
	
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
	
	public abstract boolean isSubPackage(String name);
	
	public Package resolvePackage(String name)
	{
		Package pack = this.packages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		int index = name.indexOf('.');
		if (index >= 0)
		{
			String internal = name.replace('.', '/');
			if (this.isSubPackage(internal))
			{
				String s = name.substring(0, index);
				pack = this.resolvePackage2(s);
				
				if (pack == null)
				{
					return null;
				}
				
				do
				{
					int index1 = name.indexOf('.', index + 1);
					int index2 = index1 >= 0 ? index1 : name.length();
					s = name.substring(index + 1, index2);
					pack = pack.createSubPackage(s);
					if (pack == null)
					{
						return null;
					}
					index = index1;
				}
				while (index >= 0);
				return pack;
			}
			
			return null;
		}
		if (this.isSubPackage(name))
		{
			pack = new ExternalPackage(Package.rootPackage, name, this);
			this.packages.put(name, pack);
			return pack;
		}
		return null;
	}
	
	public Package resolvePackage2(String name)
	{
		Package pack = this.packages.get(name);
		if (pack != null)
		{
			return pack;
		}
		if (this.isSubPackage(name))
		{
			pack = new ExternalPackage(Package.rootPackage, name, this);
			this.packages.put(name, pack);
			return pack;
		}
		return null;
	}
	
	public abstract InputStream getInputStream(String fileName);
	
	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}
}
