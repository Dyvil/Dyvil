package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.bytecode.ClassReader;

public class JarPackage extends Package
{
	public File	jarFile;
	
	public JarPackage(Package parent, String name, File jarFile)
	{
		super(parent, name);
		this.jarFile = jarFile;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		IClass iclass = super.resolveClass(name);
		if (iclass == null)
		{
			try (JarFile jarFile = new JarFile(this.jarFile, false, ZipFile.OPEN_READ))
			{
				String internalName = this.internalName + name + ".class";
				JarEntry entry = (JarEntry) jarFile.getEntry(internalName);
				if (entry != null)
				{
					iclass = ClassReader.loadClass(jarFile.getInputStream(entry), false);
					this.classes.add(iclass);
				}
			}
			catch (IOException ex)
			{	
				
			}
		}
		return iclass;
	}
}
