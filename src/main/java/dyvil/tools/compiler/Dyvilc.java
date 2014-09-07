package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static Dyvilc				instance;
	
	protected CompilerConfig			config;
	
	public Map<File, CompilationUnit>	compilationUnits	= new HashMap();
	
	public Dyvilc(CompilerConfig config)
	{
		this.config = config;
	}
	
	public static void main(String[] args)
	{
		File file = new File(args[0]);
		try
		{
			String s = new String(Files.readAllBytes(file.toPath()));
			
			CompilerConfig config = new CompilerConfig();
			CodeParser.instance.parse(new ConfigParser(config), s);
			
			instance = new Dyvilc(config);
			instance.compile();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void compile()
	{
		System.out.println("Compiling " + this.config.sourceDir.getAbsolutePath() + " to " + this.config.outputDir.getAbsolutePath());
		
		this.compile(this.config.sourceDir, this.config.outputDir);
	}
	
	public void compile(File source, File output)
	{
		if (!source.exists())
		{
			System.out.println(source.getPath() + " does not exist.");
		}
		else if (source.isDirectory())
		{
			for (String s : source.list())
			{
				this.compile(new File(source, s), new File(output, s));
			}
		}
		else
		{
			CompilationUnit unit = CodeParser.compilationUnit(readFile(source));
			System.out.println(unit);
			this.compilationUnits.put(source, unit);
		}
	}
	
	public static String readFile(File file)
	{
		try
		{
			return new String(Files.readAllBytes(file.toPath()));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
