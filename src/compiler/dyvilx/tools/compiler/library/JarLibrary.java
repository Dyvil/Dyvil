package dyvilx.tools.compiler.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class JarLibrary extends Library
{
	// =============== Fields ===============

	private FileSystem jarFileSystem;

	// =============== Constructors ===============

	public JarLibrary(File file)
	{
		super(file);
	}

	// =============== Methods ===============

	// --------------- Loading and Unloading ---------------

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
		catch (IOException ignored)
		{
		}
	}

	// --------------- Access ---------------

	@Override
	public boolean isSubPackage(String directory)
	{
		return Files.exists(this.jarFileSystem.getPath(directory), EMPTY_LINK_OPTIONS);
	}

	@Override
	public InputStream getInputStream(String fileName)
	{
		final Path path = this.jarFileSystem.getPath(fileName);
		if (!Files.exists(path, EMPTY_LINK_OPTIONS))
		{
			return null;
		}

		try
		{
			return Files.newInputStream(path, EMPTY_LINK_OPTIONS);
		}
		catch (IOException ignored)
		{
			return null;
		}
	}

	// --------------- Discovery ---------------

	@Override
	public Stream<Path> listPaths(String directory)
	{
		try
		{
			return Files.list(this.jarFileSystem.getPath('/' + directory));
		}
		catch (IOException e)
		{
			return Stream.empty();
		}
	}
}
