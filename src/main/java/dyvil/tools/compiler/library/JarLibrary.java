package dyvil.tools.compiler.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import dyvil.tools.compiler.ast.structure.ExternalPackage;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.ClassFormat;

public class JarLibrary extends Library
{
	private JarFile		jarFile;
	private Set<String>	packages	= new HashSet();
	
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
				int index = name.lastIndexOf('/');
				if (index != -1)
				{
					this.packages.add(name.substring(0, index));
				}
			}
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		String internalName = ClassFormat.packageToInternal(name);
		if (this.packages.contains(internalName))
		{
			ExternalPackage pack = new ExternalPackage(Package.rootPackage, name, this);
			Package.rootPackage.addSubPackage(pack);
			return pack;
		}
		return null;
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
