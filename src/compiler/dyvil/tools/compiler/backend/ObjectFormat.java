package dyvil.tools.compiler.backend;

import dyvil.io.FileUtils;
import dyvil.io.StringPoolReader;
import dyvil.io.StringPoolWriter;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.HeaderUnit;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.lang.I18n;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class ObjectFormat
{
	private static final int FILE_VERSION = 1;

	public static void write(DyvilCompiler compiler, File file, IHeaderUnit header)
	{
		try
		{
			FileUtils.create(file);

			try (StringPoolWriter writer = new StringPoolWriter(new BufferedOutputStream(new FileOutputStream(file))))
			{
				writer.writeShort(FILE_VERSION);
				header.write(writer);

				// Bytes are written when the StringPoolWriter closes
			}
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			compiler.error(I18n.get("compile.object", file), ex);
		}
	}

	public static HeaderUnit read(DyvilCompiler compiler, InputStream is, HeaderUnit header)
	{
		try (StringPoolReader reader = new StringPoolReader(is))
		{
			final int fileVersion = reader.readShort();
			if (fileVersion > FILE_VERSION)
			{
				throw new IllegalStateException("Unknown Dyvil Header File Version: " + fileVersion);
			}
			header.read(reader);
			return header;
		}
		catch (Throwable ex)
		{
			compiler.error("HeaderFile", "read", ex);
		}
		return null;
	}
}
