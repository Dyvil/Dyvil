package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static final boolean	parseStack	= false;
	
	public static Dyvilc		instance;
	
	protected CompilerConfig	config;
	protected CompilerState		state;
	
	public static ParserManager	parser		= new ParserManager();
	
	public Dyvilc(CompilerConfig config)
	{
		this.config = config;
	}
	
	public static void main(String[] args)
	{
		CodeFile file = new CodeFile(args[0]);
		
		CompilerConfig config = new CompilerConfig();
		parser.parse(file, new ConfigParser(config));
		
		instance = new Dyvilc(config);
		instance.compile();
	}
	
	public void compile()
	{
		File sourceDir = this.config.sourceDir;
		File outputDir = this.config.outputDir;
		Package root = Package.rootPackage;
		System.out.println("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		
		for (String s : sourceDir.list())
		{
			this.compile(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		List<CompilationUnit> units = new ArrayList(root.subPackages.size());
		for (Package pack : root.subPackages)
		{
			for (CompilationUnit unit : pack.units)
			{
				units.add(unit);
			}
		}
		
		for (CompilerState state : CompilerState.values())
		{
			state.apply(units, null);
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
			CompilationUnit unit = new CompilationUnit(pack, (CodeFile) source);
			pack.addCompilationUnit(unit);
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
