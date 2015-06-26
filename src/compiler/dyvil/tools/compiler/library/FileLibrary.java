package dyvil.tools.compiler.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class FileLibrary extends Library
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
	public void unloadLibrary()
	{
	}
	
	@Override
	public boolean isSubPackage(String name)
	{
		return new File(this.file, name).exists();
	}
	
	@Override
	public InputStream getInputStream(String fileName)
	{
		File file = new File(this.file, fileName);
		if (file.exists())
		{
			try
			{
				return new FileInputStream(file);
			}
			catch (FileNotFoundException ex)
			{
			}
		}
		return null;
	}
}
