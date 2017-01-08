package dyvil.io;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
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
 * The <b>FileUtils</b> class can be used for several {@link File} -related operations
 * such as writing or reading the file both as a String or as a List of Strings or recursively deleting directories.
 */
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
	public static @NonNull Pattern antToRegex(@NonNull String pattern)
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

	public static boolean matches(@NonNull String filename, @NonNull String pattern)
	{
		return antToRegex(pattern).matcher(filename).find();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryCreate(@NonNull File file)
	{
		try
		{
			create(file);
			return true;
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void create(@NonNull File file) throws IOException
	{
		if (file.exists())
		{
			return;
		}

		final File parent = file.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs())
		{
			throw new IOException("Could not create parent directory: " + parent);
		}
		//noinspection ResultOfMethodCallIgnored
		file.createNewFile();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWrite(@NonNull File file, @NonNull String text)
	{
		return tryWrite(file, text.getBytes());
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWrite(@NonNull File file, byte @NonNull [] bytes)
	{
		try
		{
			write(file, bytes);
			return true;
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void write(@NonNull File file, byte @NonNull [] bytes) throws IOException
	{
		create(file);
		Files.write(file.toPath(), bytes);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean tryWriteLines(@NonNull File file, @NonNull List<@NonNull String> lines)
	{
		try
		{
			writeLines(file, lines);
			return true;
		}
		catch (IOException ignored)
		{
			return false;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static void writeLines(@NonNull File file, @NonNull List<@NonNull String> lines) throws IOException
	{
		create(file);
		Files.write(file.toPath(), lines, Charset.defaultCharset());
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
	public static @Nullable String tryRead(@NonNull File file)
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
	public static @NonNull String read(@NonNull File file) throws IOException
	{
		byte[] bytes = Files.readAllBytes(file.toPath());
		return new String(bytes);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static @Nullable List<@NonNull String> tryReadLines(@NonNull File file)
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
	public static @NonNull List<@NonNull String> readLines(@NonNull File file) throws IOException
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
	public static boolean delete(@NonNull File file)
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
	public static boolean delete(@NonNull File file, int maxDepth)
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
}
