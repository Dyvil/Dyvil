package dyvilx.tools.compiler.library;

import dyvil.reflect.ReflectUtils;
import dyvil.reflect.Variance;

import java.io.File;
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
		javaLibraryLocation = getFileLocation(String.class);
		dyvilLibraryLocation = getFileLocation(Variance.class);

		dyvilLibrary = load(dyvilLibraryLocation);
		javaLibrary = load(javaLibraryLocation);
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

	@Deprecated
	public static Library tryLoad(File file)
	{
		return load(file);
	}

	public static Library load(File file)
	{
		if (file == null || !file.exists())
		{
			return null;
		}

		if (file.isDirectory())
		{
			return new FileLibrary(file);
		}
		if (file.getPath().endsWith(".jar"))
		{
			return new JarLibrary(file);
		}

		return null;
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

	// --------------- Package Discovery ---------------

	public Stream<Path> listPackagePaths(String directory)
	{
		return this.listPaths(directory).filter(Files::isDirectory);
	}

	public Stream<String> listPackageNames(String directory)
	{
		return this.listPackagePaths(directory).map(p -> p.getFileName().toString());
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
		return this.listFilePaths(directory).map(p -> p.getFileName().toString());
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}
}
