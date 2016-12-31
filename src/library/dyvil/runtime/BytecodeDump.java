package dyvil.runtime;

import dyvil.annotation.internal.NonNull;
import dyvil.io.FileUtils;

import java.io.File;

public class BytecodeDump
{
	// TODO add system property
	private static final File dumpDirectory = new File("runtimebin");

	@SuppressWarnings("ConstantConditions")
	public static void dump(byte @NonNull [] bytes, @NonNull String className)
	{
		if (dumpDirectory != null)
		{
			File dumpFile = new File(dumpDirectory, className.replace('/', File.separatorChar).concat(".class"));
			FileUtils.tryWrite(dumpFile, bytes);
		}
	}
}
