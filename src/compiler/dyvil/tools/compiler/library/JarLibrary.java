package dyvil.tools.compiler.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
			this.jarFileSystem = FileSystems.newFileSystem(this.file.toPath(), null);
		}
		catch (Exception ex)
		{
			System.err.println("Failed to initialize JAR library " + this.file);
			ex.printStackTrace();
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
	public boolean isSubPackage(String internal)
	{
		return Files.exists(this.jarFileSystem.getPath(internal), emptyLinkOptions);
	}

	@Override
	public InputStream getInputStream(String fileName)
	{
		final Path path = this.jarFileSystem.getPath(fileName);
		if (!Files.exists(path, emptyLinkOptions))
		{
			return null;
		}

		try
		{
			return Files.newInputStream(path, emptyLinkOptions);
		}
		catch (IOException ignored)
		{
			return null;
		}
	}
}
