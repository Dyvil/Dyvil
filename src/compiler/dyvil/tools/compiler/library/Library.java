package dyvil.tools.compiler.library;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.structure.Package;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.LinkOption;

public abstract class Library
{
	public static final File javaLibraryLocation;
	public static final File dyvilLibraryLocation;

	public static final Library dyvilLibrary;
	public static final Library javaLibrary;

	private static File getFileLocation(Class<?> klass)
	{
		try
		{
			return ReflectUtils.getFileLocation(klass);
		}
		catch (ClassNotFoundException ex)
		{
			return null;
		}
	}

	static
	{
		javaLibraryLocation = getFileLocation(java.lang.String.class);
		dyvilLibraryLocation = getFileLocation(dyvil.lang.Void.class);

		dyvilLibrary = tryLoad(dyvilLibraryLocation);
		javaLibrary = tryLoad(javaLibraryLocation);
	}

	protected static final LinkOption[] emptyLinkOptions = {};

	protected final File file;
	protected final Map<String, Package> packages = new HashMap<>();

	protected Library(File file)
	{
		this.file = file;
	}

	public static Library tryLoad(File file)
	{
		try
		{
			return load(file);
		}
		catch (FileNotFoundException ex)
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

	public abstract void loadLibrary();

	public abstract void unloadLibrary();

	public URL getURL() throws MalformedURLException
	{
		return this.file.toURI().toURL();
	}

	public abstract boolean isSubPackage(String internal);

	public Package resolvePackage(String internal)
	{
		Package pack = this.packages.get(internal);
		if (pack != null)
		{
			return pack;
		}

		if (!this.isSubPackage(internal))
		{
			return null;
		}

		int currentIndex = internal.indexOf('/');
		if (currentIndex < 0)
		{
			return Package.rootPackage.createSubPackage(internal);
		}

		String currentPart = internal.substring(0, currentIndex);
		pack = Package.rootPackage.createSubPackage(currentPart);

		if (pack == null)
		{
			return null;
		}

		do
		{
			int endIndex = internal.indexOf('/', currentIndex + 1);
			if (endIndex < 0)
			{
				endIndex = internal.length();
			}
			if (endIndex - currentIndex <= 0)
			{
				break;
			}

			currentPart = internal.substring(currentIndex + 1, endIndex);
			pack = pack.createSubPackage(currentPart);
			if (pack == null)
			{
				return null;
			}
			currentIndex = endIndex;
		}
		while (true);

		return pack;
	}

	public abstract InputStream getInputStream(String fileName);

	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		this.unloadLibrary();
	}
}
