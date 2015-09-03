package dyvil.tools.compiler.library;

import java.io.*;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.util.None;
import dyvil.util.Option;

public final class FileLibrary extends Library
{
	protected final Map<String, File> fileMap = new HashMap();
	
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
	
	private File getFile(String name)
	{
		Option<File> option = this.fileMap.getOption(name);
		if (option != None.instance)
		{
			return option.$bang();
		}
		
		String path = name.replace('/', File.separatorChar);
		File file = new File(this.file, path);
		if (!file.exists())
		{
			this.fileMap.put(name, null);
			return null;
		}
		
		try
		{
			if (!file.getCanonicalPath().endsWith(path))
			{
				this.fileMap.put(name, null);
				return null;
			}
		}
		catch (IOException ex)
		{
			System.err.println("Failed to get File Library location for " + name + " in " + this.file);
			System.err.println("Path: " + path);
			System.err.println("File: " + file);
			ex.printStackTrace();
		}
		
		this.fileMap.put(name, file);
		return file;
	}
	
	@Override
	public boolean isSubPackage(String name)
	{
		return this.getFile(name) != null;
	}
	
	@Override
	public InputStream getInputStream(String fileName)
	{
		File file = this.getFile(fileName);
		if (file != null)
		{
			try
			{
				return new FileInputStream(file);
			}
			catch (FileNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}
}
