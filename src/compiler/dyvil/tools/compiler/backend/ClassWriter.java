package dyvil.tools.compiler.backend;

import java.io.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.config.CompilerConfig;

public class ClassWriter
{
	public static void createFile(File file)
	{
		try
		{
			if (!file.exists())
			{
				File parent = file.getParentFile();
				if (parent != null)
				{
					parent.mkdirs();
				}
				file.createNewFile();
			}
		}
		catch (Exception ex)
		{
			DyvilCompiler.logger.throwing("ClassWriter", "createFile", ex);
		}
	}
	
	public static void saveClass(File file, IClass iclass)
	{
		createFile(file);
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
		{
			jdk.internal.org.objectweb.asm.ClassWriter writer = new jdk.internal.org.objectweb.asm.ClassWriter(Opcodes.ASM5);
			iclass.write(writer);
			writer.visitEnd();
			byte[] bytes = writer.toByteArray();
			os.write(bytes, 0, bytes.length);
		}
		catch (Exception ex)
		{
			DyvilCompiler.logger.throwing("ClassWriter", "saveClass", ex);
		}
	}
	
	public static void generateJAR(List<File> files)
	{
		CompilerConfig config = DyvilCompiler.config;
		String fileName = config.getJarName();
		
		File output = new File(fileName);
		createFile(output);
		
		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.putValue("Name", config.jarName);
		attributes.putValue("Version", config.jarVersion);
		attributes.putValue("Vendor", config.jarVendor);
		attributes.putValue("Created-By", "Dyvil Compiler");
		attributes.put(Attributes.Name.MAIN_CLASS, config.mainType);
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		jar(files, output, manifest);
	}
	
	public static void jar(List<File> files, File output, Manifest manifest)
	{
		String outputDir = DyvilCompiler.config.outputDir.getAbsolutePath();
		int len = outputDir.length();
		if (!outputDir.endsWith("/"))
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
			DyvilCompiler.logger.throwing("ClassWriter", "jar", ex);
		}
	}
	
	public static void createEntry(File input, JarOutputStream jos, String name)
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
			DyvilCompiler.logger.throwing("ClassWriter", "createEntry", ex);
		}
	}
}
