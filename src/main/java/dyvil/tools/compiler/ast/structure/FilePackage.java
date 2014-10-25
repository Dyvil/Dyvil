package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.bytecode.ClassReader;

public class FilePackage extends Package
{
	public File	file;
	
	public FilePackage(Package parent, String name, File file)
	{
		super(parent, name);
		this.file = file;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		IClass iclass = super.resolveClass(name);
		if (iclass == null)
		{
			try
			{
				File f = new File(this.file, ClassReader.classFile(name));
				iclass = ClassReader.loadClass(new FileInputStream(f), false);
				this.classes.add(iclass);
			}
			catch (FileNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}
		return iclass;
	}
}
