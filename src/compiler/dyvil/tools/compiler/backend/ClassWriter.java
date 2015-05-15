package dyvil.tools.compiler.backend;

import java.io.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import dyvil.io.FileUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.config.CompilerConfig;

public class ClassWriter extends org.objectweb.asm.ClassWriter
{
	public ClassWriter()
	{
		super(DyvilCompiler.asmVersion);
	}
	
	public ClassWriter(int api)
	{
		super(api);
	}
	
	public static void compile(File file, IClassCompilable iclass)
	{
		FileUtils.createFile(file);
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
		{
			ClassWriter writer = new ClassWriter();
			iclass.write(writer);
			writer.visitEnd();
			byte[] bytes = writer.toByteArray();
			os.write(bytes, 0, bytes.length);
		}
		catch (Throwable ex)
		{
			DyvilCompiler.logger.throwing("ClassWriter", "compile", ex);
		}
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
		attributes.putValue("Name", config.jarName);
		attributes.putValue("Version", config.jarVersion);
		attributes.putValue("Vendor", config.jarVendor);
		attributes.putValue("Created-By", "Dyvil Compiler");
		attributes.put(Attributes.Name.MAIN_CLASS, config.mainType);
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		
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
			DyvilCompiler.logger.throwing("ClassWriter", "createEntry", ex);
		}
	}
}
