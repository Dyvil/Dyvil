package dyvil.tools.compiler.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JarLibrary extends Library
{
	private FileSystem jarFileSystem;
	
	public JarLibrary(File file)
	{
		super(file);
	}
	
	@Override
	public void loadLibrary()
	{
		try
		{
			URI uri = URI.create("jar:file:" + this.file.getAbsolutePath());
			this.jarFileSystem = FileSystems.newFileSystem(uri, this.packages);
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public void unloadLibrary()
	{
		try
		{
			this.jarFileSystem.close();
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public boolean isSubPackage(String name)
	{
		return Files.exists(this.jarFileSystem.getPath(name), emptyLinkOptions);
	}
	
	@Override
	public InputStream getInputStream(String fileName)
	{
		Path path = this.jarFileSystem.getPath(fileName);
		if (Files.exists(path, emptyLinkOptions))
		{
			try
			{
				return Files.newInputStream(path, emptyLinkOptions);
			}
			catch (IOException ex)
			{
			}
		}
		return null;
	}
}
