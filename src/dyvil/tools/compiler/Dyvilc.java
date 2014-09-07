package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static Dyvilc		instance;
	
	protected CompilerConfig	config;
	
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
		System.out.println("Compiling...");
		System.out.println("Compiler Config: " + this.config.toString());
	}
}
