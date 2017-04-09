package dyvil.runtime;

import dyvil.annotation.internal.NonNull;
import dyvil.io.FileUtils;

import java.io.File;

public class BytecodeDump
{
	private static final String dumpProperty = System.getProperty("dyvil.bytecode.dump");
	private static final File dumpDirectory = dumpProperty == null ? null : new File(dumpProperty);

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
