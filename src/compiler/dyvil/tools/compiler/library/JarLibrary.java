package dyvil.tools.compiler.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class JarLibrary extends Library
{
	private JarFile		jarFile;
	private Set<String>	packageNames	= new TreeSet();
	
	public JarLibrary(File file)
	{
		super(file);
	}
	
	@Override
	public void loadLibrary()
	{
		try
		{
			this.jarFile = new JarFile(this.file);
			
			Enumeration e = this.jarFile.entries();
			while (e.hasMoreElements())
			{
				JarEntry entry = (JarEntry) e.nextElement();
				String name = entry.getName();
				int index = name.length();
				while ((index = name.lastIndexOf('/', index - 1)) != -1)
				{
					if (!this.packageNames.add(name.substring(0, index)))
					{
						break;
					}
				}
			}
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public boolean isSubPackage(String name)
	{
		return this.packageNames.contains(name);
	}
	
	@Override
	public InputStream getInputStream(String fileName)
	{
		ZipEntry entry = this.jarFile.getEntry(fileName);
		if (entry != null)
		{
			try
			{
				return this.jarFile.getInputStream(entry);
			}
			catch (IOException ex)
			{
			}
		}
		return null;
	}
}
