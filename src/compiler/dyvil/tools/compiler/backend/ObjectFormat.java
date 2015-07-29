package dyvil.tools.compiler.backend;

import java.io.*;

import dyvil.io.FileUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;

public class ObjectFormat
{
	private static final int FILE_VERSION = 1;
	
	public static void write(File file, IDyvilHeader header)
	{
		if (!FileUtils.createFile(file))
		{
			DyvilCompiler.error("Error during compilation of '" + file + "': could not create file");
			return;
		}
		
		try (ObjectWriter writer = new ObjectWriter(); OutputStream fo = new BufferedOutputStream(new FileOutputStream(file)))
		{
			writer.writeShort(FILE_VERSION);
			header.write(writer);
			
			// Directly write the DyO byte code to the file output to save some
			// unnecessary byte array allocations
			writer.writeTo(fo);
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
		try (ObjectReader reader = new ObjectReader(new DataInputStream(is)))
		{
			int fileVersion = reader.readShort();
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
