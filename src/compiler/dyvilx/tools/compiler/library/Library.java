package dyvilx.tools.compiler.library;

import dyvil.reflect.ReflectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class Library
{
	// =============== Constants ===============

	protected static final LinkOption[] EMPTY_LINK_OPTIONS = {};

	// =============== Static Final Fields ===============

	public static final File javaLibraryLocation;
	public static final File dyvilLibraryLocation;

	public static final Library dyvilLibrary;
	public static final Library javaLibrary;

	// =============== Static Initializers ===============

	static
	{
		javaLibraryLocation = getFileLocation(java.lang.String.class);
		dyvilLibraryLocation = getFileLocation(dyvil.reflect.Variance.class);

		dyvilLibrary = tryLoad(dyvilLibraryLocation);
		javaLibrary = tryLoad(javaLibraryLocation);
	}

	// =============== Fields ===============

	protected final File file;

	// =============== Constructors ===============

	protected Library(File file)
	{
		this.file = file;
	}

	// =============== Static Methods ===============

	private static File getFileLocation(Class<?> klass)
	{
		try
		{
			return ReflectUtils.getFileLocation(klass);
		}
		catch (ClassNotFoundException ignored)
		{
			return null;
		}
	}

	public static Library tryLoad(File file)
	{
		try
		{
			return load(file);
		}
		catch (FileNotFoundException ignored)
		{
			return null;
		}
	}

	public static Library load(File file) throws FileNotFoundException
	{
		if (file == null)
		{
			return null;
		}

		if (file.isDirectory())
		{
			return new FileLibrary(file);
		}
		else if (file.getPath().endsWith(".jar"))
		{
			return new JarLibrary(file);
		}

		final String error = "Invalid Library File: " + file.getAbsolutePath() + (file.exists() ?
			                                                                          " (Unsupported Format)" :
			                                                                          " (File does not exist)");
		throw new FileNotFoundException(error);
	}

	// =============== Properties ===============

	public File getFile()
	{
		return this.file;
	}

	// =============== Methods ===============

	// --------------- Loading and Unloading ---------------

	public abstract void loadLibrary();

	public abstract void unloadLibrary();

	public abstract Stream<Path> listPaths(String directory);

	// --------------- Package Access ---------------

	public abstract boolean isSubPackage(String directory);

	// --------------- Package Discovery ---------------

	public Stream<Path> listPackagePaths(String directory)
	{
		return this.listPaths(directory).filter(Files::isDirectory);
	}

	public Stream<String> listPackageNames(String directory)
	{
		return this.listPackagePaths(directory).map(p -> p.toFile().getName());
	}

	// --------------- File Access ---------------

	public abstract InputStream getInputStream(String fileName);

	// --------------- File Discovery ---------------

	public Stream<Path> listFilePaths(String directory)
	{
		return this.listPaths(directory).filter(Files::isRegularFile);
	}

	public Stream<String> listFileNames(String directory)
	{
		return this.listFilePaths(directory).map(p -> p.toFile().getName());
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}
}
