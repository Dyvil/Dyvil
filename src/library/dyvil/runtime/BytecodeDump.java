package dyvil.runtime;

import dyvil.io.FileUtils;

import java.io.File;

public class BytecodeDump
{
	private static final File dumpDirectory = new File("runtimebin");

	@SuppressWarnings("ConstantConditions")
	public static void dump(byte[] bytes, String className)
	{
		if (dumpDirectory != null)
		{
			File dumpFile = new File(dumpDirectory, className.replace('/', File.separatorChar).concat(".class"));
			FileUtils.tryWrite(dumpFile, bytes);
		}
	}
}
