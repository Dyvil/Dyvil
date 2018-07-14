package dyvilx.tools.compiler.backend.classes;

import dyvil.collection.List;
import dyvil.io.FileUtils;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilable;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.compiler.lang.I18n;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

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
			FileUtils.create(file);

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

	public static void generateJAR(DyvilCompiler compiler)
	{
		final List<File> files = compiler.fileFinder.files;
		final CompilerConfig config = compiler.config;
		final File output = new File(config.getJarName());

		FileUtils.tryCreate(output);

		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.putValue("Name", config.getJarName());
		attributes.putValue("Version", config.getJarVersion());
		attributes.putValue("Vendor", config.getJarVendor());
		attributes.putValue("Created-By", "Dyvil Compiler");
		attributes.put(Attributes.Name.MAIN_CLASS, config.getMainType());
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

		String outputDir = config.getOutputDir().getAbsolutePath();
		int len = outputDir.length();
		if (outputDir.charAt(len - 1) != File.separatorChar)
		{
			len++;
		}

		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(output), manifest))
		{
			for (File file : files)
			{
				if (file.exists())
				{
					String name = file.getAbsolutePath().substring(len);
					createEntry(compiler, file, jos, name);
				}
			}

			jos.flush();
		}
		catch (Exception ex)
		{
			compiler.error("ClassWriter", "jar", ex);
		}
	}

	private static void createEntry(DyvilCompiler compiler, File input, JarOutputStream jos, String name)
	{
		try (FileInputStream fis = new FileInputStream(input))
		{
			byte[] buffer = new byte[1024];
			JarEntry entry = new JarEntry(name);
			jos.putNextEntry(entry);

			int len;
			while ((len = fis.read(buffer)) > 0)
			{
				jos.write(buffer, 0, len);
			}
		}
		catch (Exception ex)
		{
			compiler.error("ClassWriter", "createEntry", ex);
		}
	}
}
