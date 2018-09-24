package dyvilx.tools.compiler.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
	public boolean isSubPackage(String directoryName)
	{
		return this.getFile(directoryName).isDirectory();
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
}
