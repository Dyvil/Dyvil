package dyvil.tools.compiler.backend;

import dyvil.io.FileUtils;
import dyvil.io.StringPoolReader;
import dyvil.io.StringPoolWriter;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;

import java.io.*;

public class ObjectFormat
{
	private static final int FILE_VERSION = 1;

	public static void write(File file, IDyvilHeader header)
	{
		if (!FileUtils.create(file))
		{
			DyvilCompiler.error("Error during compilation of '" + file + "': could not create file");
			return;
		}
		
		try (StringPoolWriter writer = new StringPoolWriter(new BufferedOutputStream(new FileOutputStream(file))))
		{
			writer.writeShort(FILE_VERSION);
			header.write(writer);

			// Bytes are written when the StringPoolWriter closes
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			DyvilCompiler.warn("Error during compilation of '" + file + "': " + ex.getLocalizedMessage());
			DyvilCompiler.error("ClassWriter", "compile", ex);
			return;
		}
	}
	
	public static DyvilHeader read(InputStream is, DyvilHeader header)
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
			DyvilCompiler.error("HeaderFile", "read", ex);
		}
		return null;
	}
}
