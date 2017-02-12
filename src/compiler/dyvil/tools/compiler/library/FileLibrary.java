package dyvil.tools.compiler.library;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.util.Option;

import java.io.*;

public final class FileLibrary extends Library
{
	protected final Map<String, File> fileMap = new HashMap<>();

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
		final Option<File> option = this.fileMap.getOption(name);
		if (option.isPresent())
		{
			return option.get();
		}

		File file = new File(this.file, name);
		if (!file.exists())
		{
			this.fileMap.put(name, null);
			return null;
		}

		try
		{
			if (!file.getCanonicalPath().endsWith(name))
			{
				this.fileMap.put(name, null);
				return null;
			}
		}
		catch (IOException ex)
		{
			System.err.println("Failed to get File Library location for " + name + " in " + this.file);
			System.err.println("Path: " + name);
			System.err.println("File: " + file);
			ex.printStackTrace();
		}

		this.fileMap.put(name, file);
		return file;
	}

	@Override
	public boolean isSubPackage(String internal)
	{
		return this.getFile(internal) != null;
	}

	@Override
	public InputStream getInputStream(String fileName)
	{
		final File file = this.getFile(fileName);
		if (file == null)
		{
			return null;
		}

		try
		{
			return new FileInputStream(file);
		}
		catch (FileNotFoundException ignored)
		{
			return null;
		}
	}
}
