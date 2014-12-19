package dyvil.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * {@code FileUtils} and its members can be used for several operations related
 * to files on the hard drive.
 * 
 * @author Clashsoft
 */
public class FileUtils
{
	/**
	 * Writes the given {@link String} {@code text} to the given {@link File}
	 * {@code file}. This operation attempts to override an existing file, and
	 * creates a new file if no existing file was found. If an exception occurs,
	 * its stack trace is printed.
	 * 
	 * @param file
	 *            the file
	 * @param text
	 *            the text
	 * @return true, if successful
	 */
	public static boolean write(File file, String text)
	{
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
			
			byte[] bytes = text.getBytes();
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
	 * creates a new file if no existing file was found. If an exception occurs,
	 * its stack trace is printed.
	 * 
	 * @param file
	 *            the file
	 * @param lines
	 *            the lines
	 * @return true, if successful
	 */
	public static boolean writeLines(File file, List<String> lines)
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
	 * {@code null}. If any other {@link IOException} occurs, its stack trace is
	 * printed.
	 * 
	 * @param file
	 *            the file
	 * @return the file content
	 */
	public static String read(File file)
	{
		try
		{
			if (!file.exists())
			{
				return null;
			}
			
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
	public static List<String> readLines(File file)
	{
		try
		{
			if (!file.exists())
			{
				return null;
			}
			
			return Files.readAllLines(file.toPath(), Charset.defaultCharset());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the AppData directory of the host OS. Common paths are:
	 * <ul>
	 * <li>Windows: {@code %appdata%/}
	 * <li>Mac OS: {@code $username$/Library/Application Support/}
	 * <li>Linux: {@code $username$}
	 * <li>Every other OS: System Property {@code user.dir}
	 * </ul>
	 * 
	 * @return the AppData directory
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
