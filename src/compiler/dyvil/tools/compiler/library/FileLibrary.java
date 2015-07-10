package dyvil.tools.compiler.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
		File f = new File(this.file, name);
		if (!f.exists())
		{
			return false;
		}
		
		try
		{
			if (f.getCanonicalPath().endsWith(name))
			{
				return true;
			}
		}
		catch (IOException ex)
		{
		}
		return false;
	}
	
	@Override
	public InputStream getInputStream(String fileName)
	{
		File file = new File(this.file, fileName);
		if (!file.exists())
		{
			return null;
		}
		
		try
		{
			if (file.getCanonicalPath().endsWith(fileName))
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
