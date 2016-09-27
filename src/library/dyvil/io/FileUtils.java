package dyvil.io;

import dyvil.annotation.Utility;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * The {@linkplain Utility utility interface} <b>FileUtils</b> can be used for several {@link File} -related operations
 * such as writing or reading the file both as a String or as a List of Strings or recursively deleting directories.
 *
 * @author Clashsoft
 * @version 1.0
 */
@Utility(File.class)
public final class FileUtils
{
	private FileUtils()
	{
		// no instances
	}

	/**
	 * Converts an Ant-style filename pattern to a Regular Expression Pattern
	 *
	 * @param pattern
	 * 	the filename pattern
	 *
	 * @return the Regular Expression pattern
	 */
	public static Pattern antToRegex(String pattern)
	{
		final int length = pattern.length();
		StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; i++)
		{
			char c = pattern.charAt(i);
			switch (c)
			{
			case '?':
				builder.append('.');
				continue;
			case '*':
				if (i + 1 < length && pattern.charAt(i + 1) == '*')
				{
					builder.append(".*");
					continue;
				}
				builder.append("[^/]*");
				continue;
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '.':
			case '^':
			case '$':
			case '|':
				builder.append('\\').append(c);
				continue;
			}
			builder.append(c);
		}

		return Pattern.compile(builder.toString());
	}

	public static boolean matches(String filename, String pattern)
	{
		return antToRegex(pattern).matcher(filename).find();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryCreate(File file)
	{
		try
		{
			return create(file);
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean create(File file) throws IOException
	{
		if (file.exists())
		{
			return true;
		}

		final File parent = file.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs())
		{
			return false;
		}
		// return true if the file was created successfully
		return !file.createNewFile();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWrite(File file, String text)
	{
		return tryWrite(file, text.getBytes());
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWrite(File file, byte[] bytes)
	{
		try
		{
			return write(file, bytes);
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean write(File file, byte[] bytes) throws IOException
	{
		if (!create(file))
		{
			return false;
		}

		Files.write(file.toPath(), bytes);
		return true;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWriteLines(File file, List<String> lines)
	{
		try
		{
			return writeLines(file, lines);
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean writeLines(File file, List<String> lines) throws IOException
	{
		if (!create(file))
		{
			return false;
		}

		Files.write(file.toPath(), lines, Charset.defaultCharset());
		return true;
	}

	/**
	 * Reads the content of the the given {@link File} {@code file} and returns it as a {@link String}. If the {@code
	 * file} does not exist, it returns {@code null}. If any other {@link IOException} occurs, the stack trace of the
	 * exception is printed using {@link Throwable#printStackTrace()}.
	 *
	 * @param file
	 * 	the file
	 *
	 * @return the file content
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static String tryRead(File file)
	{
		if (!file.exists())
		{
			return null;
		}
		try
		{
			return read(file);
		}
		catch (IOException ignored)
		{
			return null;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String read(File file) throws IOException
	{
		byte[] bytes = Files.readAllBytes(file.toPath());
		return new String(bytes);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static List<String> tryReadLines(File file)
	{
		if (!file.exists())
		{
			return null;
		}
		try
		{
			return readLines(file);
		}
		catch (IOException ignored)
		{
			return null;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static List<String> readLines(File file) throws IOException
	{
		try (BufferedReader reader = Files.newBufferedReader(file.toPath()))
		{
			final List<String> result = new ArrayList<>();
			for (; ; )
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
	}

	/**
	 * Recursively deletes the given {@link File} {@code file}. If the {@code file} is a directory, this method deletes
	 * all sub-files of that directory by calling itself on the sub-file. Otherwise, it simply deletes the file using
	 * {@link File#delete()}.
	 *
	 * @param file
	 * 	the file to delete
	 *
	 * @return {@code true} iff the file was successfully deleted, {@code false} otherwise
	 */
	public static boolean delete(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File subFile : files)
				{
					delete(subFile);
				}
			}
		}
		return file.delete();
	}

	/**
	 * Recursively deletes the given {@link File} {@code file}. If the {@code file} is a directory, this method deletes
	 * all sub-files of that directory by calling itself on the sub-file. Otherwise, it simply deletes the file using
	 * {@link File#delete()}. The given {@code maxDepth} is used to limit the recursive process to a maximum directory
	 * depth. If it set to {@code 0}, it is ignored whether or not the given {@code file} is a directory.
	 *
	 * @param file
	 * 	the file to delete
	 * @param maxDepth
	 * 	the maximum recursion depth
	 *
	 * @return {@code true} iff the file was successfully deleted, {@code false} otherwise
	 */
	public static boolean delete(File file, int maxDepth)
	{
		if (maxDepth > 0 && file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File subFile : files)
				{
					delete(subFile, maxDepth - 1);
				}
			}
		}
		return file.delete();
	}

	/**
	 * Returns the user application data directory of the host OS. Common paths are: <ul> <li>Windows: {@code
	 * %appdata%/} <li>Mac OS: {@code $username$/Library/Application Support/} <li>Linux: {@code $username$} <li>Every
	 * other OS: System Property {@code user.dir} </ul> The {@code $username$} variable is acquired via the system
	 * property {@code user.home}.
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
