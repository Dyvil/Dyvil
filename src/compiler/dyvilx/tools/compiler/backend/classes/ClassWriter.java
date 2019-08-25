package dyvilx.tools.compiler.backend.classes;

import dyvil.io.Files;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilable;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.lang.I18n;

import java.io.*;

public class ClassWriter extends dyvilx.tools.asm.ClassWriter
{
	public ClassWriter()
	{
		super(ClassFormat.ASM_VERSION);
	}

	public ClassWriter(int api)
	{
		super(api);
	}

	public static byte[] compile(ICompilable iclass) throws Throwable
	{
		ClassWriter writer = new ClassWriter();
		iclass.write(writer);
		writer.visitEnd();
		return writer.toByteArray();
	}

	public static void save(DyvilCompiler compiler, File file, byte[] bytes)
	{
		try
		{
			Files.createRecursively(file);

			try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
			{
				os.write(bytes, 0, bytes.length);
			}
		}
		catch (IOException ex)
		{
			// If file saving fails, simply report the error.
			compiler.error(I18n.get("compile.class.save", file), ex);
		}
	}

	public static void compile(DyvilCompiler compiler, File file, ICompilable compilable)
	{
		byte[] bytes;

		// First try to compile the file to a byte array
		try
		{
			bytes = compile(compilable);
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			compiler.error(I18n.get("compile.class.bytecode", file), ex);
			return;
		}

		// If the compilation was successful, we can try to write the newly
		// created byte array to a newly created, empty file.
		save(compiler, file, bytes);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2)
	{
		assert false : "COMPUTE_FRAMES should not be used!";
		return "java/lang/Object";
	}
}
