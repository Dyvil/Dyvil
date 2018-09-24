package dyvilx.tools.compiler.library;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileLibrary extends Library
{
	// =============== Constructors ===============

	public FileLibrary(File root)
	{
		super(root);
	}

	// =============== Methods ===============

	// --------------- Loading and Unloading ---------------

	@Override
	public void loadLibrary()
	{
	}

	@Override
	public void unloadLibrary()
	{
	}

	// --------------- Access ---------------

	private File getFile(String name)
	{
		return new File(this.file, name);
	}

	@Override
	public boolean isSubPackage(String directory)
	{
		return this.getFile(directory).isDirectory();
	}

	@Override
	public InputStream getInputStream(String fileName)
	{
		try
		{
			return new FileInputStream(this.getFile(fileName));
		}
		catch (FileNotFoundException ignored)
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
			return Files.list(this.file.toPath().resolve(directory)).filter(Files::isDirectory);
		}
		catch (IOException e)
		{
			return Stream.empty();
		}
	}
}
