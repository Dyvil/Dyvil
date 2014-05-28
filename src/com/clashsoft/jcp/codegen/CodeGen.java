package com.clashsoft.jcp.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class CodeGen
{
	public static void loadClass(String className, String code)
	{
		File f = writeClass(className, code);
		compileClass(f);
		executeClass(f);
	}
	
	public static File writeClass(String name, String clazz)
	{
		System.out.println("Writing file...");
		try
		{
			File f = new File(getSaveDataFolder(), name + ".java");
			BufferedWriter writer = null;
			try
			{
				writer = new BufferedWriter(new FileWriter(f));
				writer.write(clazz);
				writer.close();
				System.out.println("Successfully written to file");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			return f;
		}
		catch (Exception ex)
		{
			System.out.println("Failed to write to file: " + ex.getMessage());
			return null;
		}
	}
	
	public static void compileClass(File file)
	{
		System.out.println("Compiling " + file.getName());
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int compilationresult = compiler.run(System.in, System.out, System.err, "-verbose", file.getPath());
		
		System.out.println("Compilation result: " + compilationresult);
	}
	
	public static void executeClass(File file)
	{
		System.out.println("Executing " + file);
		
		int slashPos = file.getPath().lastIndexOf('/');
		
		String path = file.getPath().substring(0, slashPos);
		String className = file.getPath().substring(slashPos + 1, file.getPath().lastIndexOf('.'));
		
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", path, className);
		pb.redirectErrorStream(true);
		
		try
		{
			Process p = pb.start();
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
			
			outputGobbler.start();
			errorGobbler.start();
			
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static File getSaveDataFolder()
	{
		File f = new File(getAppdataDirectory(), "codegen");
		if (!f.exists())
		{
			f.mkdirs();
		}
		return f;
	}
	
	public static String getAppdataDirectory()
	{
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN"))
			return System.getenv("APPDATA");
		else if (OS.contains("MAC"))
			return System.getProperty("user.home") + "/Library/Application Support";
		else if (OS.contains("NUX"))
			return System.getProperty("user.home");
		return System.getProperty("user.dir");
	}
}
