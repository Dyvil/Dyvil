package dyvil.runtime;

import dyvil.annotation.internal.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BytecodeDump
{
	private static final String dumpProperty  = System.getProperty("dyvil.bytecode.dump");
	private static final File   dumpDirectory = dumpProperty == null ? null : new File(dumpProperty);

	static
	{
		if (dumpDirectory != null)
		{
			//noinspection ResultOfMethodCallIgnored
			dumpDirectory.mkdirs();
		}
	}

	public static void dump(byte @NonNull [] bytes, @NonNull String className)
	{
		if (dumpDirectory != null)
		{
			File dumpFile = new File(dumpDirectory, className.replace('/', File.separatorChar).concat(".class"));
			try (FileOutputStream os = new FileOutputStream(dumpFile))
			{
				os.write(bytes);
			}
			catch (IOException ignored)
			{
			}
		}
	}
}
