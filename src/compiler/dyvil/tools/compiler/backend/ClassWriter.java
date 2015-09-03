package dyvil.tools.compiler.backend;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import dyvil.collection.List;
import dyvil.io.FileUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.config.CompilerConfig;

public class ClassWriter extends dyvil.tools.asm.ClassWriter
{
	public ClassWriter()
	{
		super(DyvilCompiler.asmVersion);
	}
	
	public ClassWriter(int api)
	{
		super(api);
	}
	
	public static byte[] compile(IClassCompilable iclass) throws Throwable
	{
		ClassWriter writer = new ClassWriter();
		iclass.write(writer);
		writer.visitEnd();
		return writer.toByteArray();
	}
	
	public static void save(File file, byte[] bytes)
	{
		if (!FileUtils.createFile(file))
		{
			DyvilCompiler.error("Error during compilation of '" + file + "': could not create file");
		}
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
		{
			os.write(bytes, 0, bytes.length);
		}
		catch (IOException ex)
		{
			// If file saving fails, simply report the error.
			DyvilCompiler.error("Error during compilation of '" + file + "': " + ex.getLocalizedMessage());
			DyvilCompiler.error("ClassWriter", "compile", ex);
		}
	}
	
	public static void compile(File file, IClassCompilable iclass)
	{
		byte[] bytes;
		
		// First try to compile the file to a byte array
		try
		{
			bytes = compile(iclass);
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			DyvilCompiler.error("Error during compilation of '" + file + "': " + ex);
			DyvilCompiler.error("ClassWriter", "compile", ex);
			return;
		}
		
		// If the compilation was successful, we can try to write the newly
		// created byte array to a newly created, empty file.
		save(file, bytes);
	}
	
	@Override
	protected String getCommonSuperClass(String type1, String type2)
	{
		assert false : "COMPUTE_FRAMES should not be used!";
		return "java/lang/Object";
	}
	
	public static void generateJAR(List<File> files)
	{
		CompilerConfig config = DyvilCompiler.config;
		String fileName = config.getJarName();
		
		File output = new File(fileName);
		FileUtils.createFile(output);
		
		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.putValue("Name", config.getJarName());
		attributes.putValue("Version", config.getJarVersion());
		attributes.putValue("Vendor", config.getJarVendor());
		attributes.putValue("Created-By", "Dyvil Compiler");
		attributes.put(Attributes.Name.MAIN_CLASS, config.getMainType());
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		
		String outputDir = DyvilCompiler.config.getOutputDir().getAbsolutePath();
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
					createEntry(file, jos, name);
				}
			}
			
			jos.flush();
		}
		catch (Exception ex)
		{
			DyvilCompiler.error("ClassWriter", "jar", ex);
		}
	}
	
	private static void createEntry(File input, JarOutputStream jos, String name)
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
			DyvilCompiler.error("ClassWriter", "createEntry", ex);
		}
	}
}
