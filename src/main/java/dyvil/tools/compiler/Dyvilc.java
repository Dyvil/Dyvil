package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static Dyvilc				instance;
	
	protected CompilerConfig			config;
	protected CompilerState				state;
	
	public Map<File, CompilationUnit>	compilationUnits	= new HashMap();
	
	public Dyvilc(CompilerConfig config)
	{
		this.config = config;
	}
	
	public static void main(String[] args)
	{
		CodeFile file = new CodeFile(args[0]);
		
		CompilerConfig config = new CompilerConfig();
		CodeParser.instance.parse(file, new ConfigParser(config));
		
		instance = new Dyvilc(config);
		instance.compile();
	}
	
	public void compile()
	{
		File sourceDir = this.config.sourceDir;
		File outputDir = this.config.outputDir;
		System.out.println("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		
		for (String s : sourceDir.list())
		{
			this.compile(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		for (CompilationUnit unit : this.compilationUnits.values())
		{
			CompilerState state = CompilerState.RESOLVE;
			state.rootPackage = Package.rootPackage;
			state.file = unit.getFile();
			
			unit.applyState(state);
			
			System.out.println("Compiled in " + unit.loadingTime + " ms");
			System.out.println(unit);
			
			state.file.printMarkers();
		}
	}
	
	public void compile(File source, File output, Package pack)
	{
		if (!source.exists())
		{
			System.out.println(source.getPath() + " does not exist.");
		}
		else if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				this.compile(new CodeFile(source, s), new File(output, s), pack.createSubPackage(name));
			}
		}
		else
		{
			CompilationUnit unit = CodeParser.compilationUnit(pack, (CodeFile) source);
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
