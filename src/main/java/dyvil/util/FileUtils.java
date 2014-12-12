package dyvil.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * A class for accessing files on the disk.
 * 
 * @author Clashsoft
 */
public class FileUtils
{
	/**
	 * Writes the given {@link String} {@code text} to the given {@link File}
	 * {@code file}.
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
	 * {@code file}.
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
	 * {@code null}.
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
	 * returns {@code null}.
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
	 * Returns the Appdata directory of the host OS. Common paths are:
	 * <ul>
	 * <li>Windows: {@code %appdata%/}
	 * <li>Mac OS: {@code $username$/Library/Application Support/}
	 * <li>Linux: {@code $username$}
	 * <li>Every other OS: System Property {@code user.dir}
	 * </ul>
	 * 
	 * @return the Appdata directory
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
