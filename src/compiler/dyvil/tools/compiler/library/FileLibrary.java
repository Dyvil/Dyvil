package dyvil.tools.compiler.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	public boolean isSubPackage(String name)
	{
		return new File(this.file, name).exists();
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
