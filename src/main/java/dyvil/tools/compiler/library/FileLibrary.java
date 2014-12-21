package dyvil.tools.compiler.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import dyvil.tools.compiler.ast.structure.ExternalPackage;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.ClassFormat;

public class FileLibrary extends Library
{
	public FileLibrary(File file)
	{
		super(file);
	}
	
	@Override
	public void loadLibrary()
	{
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		File file = new File(this.file, ClassFormat.packageToInternal(name));
		if (file.exists())
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
		try
		{
			File file = new File(this.file, fileName);
			if (file.exists())
			{
				return new FileInputStream(file);
			}
		}
		catch (IOException ex)
		{
		}
		return null;
	}
}
