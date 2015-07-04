package dyvil.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import dyvil.lang.List;

import dyvil.annotation.Utility;
import dyvil.annotation.infix;
import dyvil.collection.mutable.ArrayList;

/**
 * The {@linkplain Utility utility interface} <b>FileUtils</b> can be used for
 * several {@link File} -related operations such as writing or reading the file
 * both as a String or as a List of Strings or recursively deleting directories.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility(File.class)
public interface FileUtils
{
	/**
	 * Ensures that the given {@code file} exists. If the file does not exist in
	 * terms of it's {@link File#exists() exists()} method, the file will be
	 * created by it's {@link File#createNewFile() createNewFile()} method.
	 * Before that happens, this method ensures that the parent directory of the
	 * file exists by using it's {@link File#mkdirs() mkdirs()} method.<br>
	 * If the file exists or has been created successfully, {@code true} will be
	 * returned from this method. If any {@link IOException} occurs,
	 * {@code false} will be returned.
	 * 
	 * @param file
	 *            the file to create
	 * @return true if the file exists or has been created successfully, false
	 *         otherwise
	 */
	public static boolean createFile(File file)
	{
		try
		{
			if (!file.exists())
			{
				File parent = file.getParentFile();
				if (parent != null)
				{
					parent.mkdirs();
				}
				file.createNewFile();
			}
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}
	
	/**
	 * Writes the given {@link String} {@code text} to the given {@link File}
	 * {@code file}. This operation attempts to override an existing file, and
	 * creates a new file if no existing file exists. If an {@link IOException}
	 * occurs, the stack trace of the exception is printed using
	 * {@link Throwable#printStackTrace()}.
	 * 
	 * @param file
	 *            the file
	 * @param text
	 *            the text
	 * @return true, if successful
	 */
	
	public static @infix boolean write(File file, String text)
	{
		return write(file, text.getBytes());
	}
	
	public static @infix boolean write(File file, byte[] bytes)
	{
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
			
			Files.write(file.toPath(), bytes);
			return true;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Writes the given {@link List} {@code lines} to the given {@link File}
	 * {@code file}. This operation attempts to override an existing file, and
	 * creates a new file if no existing file exists. If an {@link IOException}
	 * occurs, the stack trace of the exception is printed using
	 * {@link Throwable#printStackTrace()}.
	 * 
	 * @param file
	 *            the file
	 * @param lines
	 *            the lines
	 * @return true, if successful
	 */
	public static @infix boolean writeLines(File file, List<String> lines)
	{
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
			
			Files.write(file.toPath(), lines, Charset.defaultCharset());
			return true;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Reads the content of the the given {@link File} {@code file} and returns
	 * it as a {@link String}. If the {@code file} does not exist, it returns
	 * {@code null}. If any other {@link IOException} occurs, the stack trace of
	 * the exception is printed using {@link Throwable#printStackTrace()}.
	 * 
	 * @param file
	 *            the file
	 * @return the file content
	 */
	public static @infix String read(File file)
	{
		if (!file.exists())
		{
			return null;
		}
		try
		{
			byte[] bytes = Files.readAllBytes(file.toPath());
			return new String(bytes);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads the content of the the given {@link File} {@code file} and returns
	 * it as a {@link List} of lines. If the {@code file} does not exist, it
	 * returns {@code null}. If any other {@link IOException} occurs, its stack
	 * trace is printed.
	 * 
	 * @param file
	 *            the file
	 * @return the file content
	 */
	public static @infix List<String> readLines(File file)
	{
		if (!file.exists())
		{
			return null;
		}
		try (BufferedReader reader = Files.newBufferedReader(file.toPath()))
		{
			List<String> result = new ArrayList();
			for (;;)
			{
				String line = reader.readLine();
				if (line == null)
				{
					break;
				}
				result.add(line);
			}
			return result;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Recursively deletes the given {@link File} {@code file}. If the
	 * {@code file} is a directory, this method deletes all sub-files of that
	 * directory by calling itself on the sub-file. Otherwise, it simply deletes
	 * the file using {@link File#delete()}.
	 * 
	 * @param file
	 *            the file to delete
	 */
	public static void delete(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File f : files)
				{
					delete(f);
				}
			}
		}
		file.delete();
	}
	
	/**
	 * Recursively deletes the given {@link File} {@code file}. If the
	 * {@code file} is a directory, this method deletes all sub-files of that
	 * directory by calling itself on the sub-file. Otherwise, it simply deletes
	 * the file using {@link File#delete()}. The given {@code maxDepth} is used
	 * to limit the recursive process to a maximum directory depth. If it set to
	 * {@code 0}, it is ignored whether or not the given {@code file} is a
	 * directory.
	 * 
	 * @param file
	 *            the file to delete
	 * @param maxDepth
	 *            the maximum recursion depth
	 */
	public static void delete(File file, int maxDepth)
	{
		if (maxDepth > 0 && file.isDirectory())
		{
			for (File f : file.listFiles())
			{
				delete(f, maxDepth - 1);
			}
		}
		file.delete();
	}
	
	/**
	 * Returns the user application data directory of the host OS. Common paths
	 * are:
	 * <ul>
	 * <li>Windows: {@code %appdata%/}
	 * <li>Mac OS: {@code $username$/Library/Application Support/}
	 * <li>Linux: {@code $username$}
	 * <li>Every other OS: System Property {@code user.dir}
	 * </ul>
	 * The {@code $username$} variable is acquired via the system property
	 * {@code user.home}.
	 * 
	 * @return the user application data directory
	 */
	public static String getAppdataDirectory()
	{
		String os = System.getProperty("os.name").toUpperCase();
		if (os.contains("WIN"))
		{
			return System.getenv("APPDATA");
		}
		else if (os.contains("MAC"))
		{
			return System.getProperty("user.home") + "/Library/Application Support";
		}
		else if (os.contains("NUX"))
		{
			return System.getProperty("user.home");
		}
		return System.getProperty("user.dir");
	}
}
