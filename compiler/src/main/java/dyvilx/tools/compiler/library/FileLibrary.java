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
	public InputStream getInputStream(String fileName)
	{
		try
		{
			return new BufferedInputStream(new FileInputStream(this.getFile(fileName)));
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
			return Files.list(this.getFile(directory).toPath());
		}
		catch (IOException e)
		{
			return Stream.empty();
		}
	}
}
