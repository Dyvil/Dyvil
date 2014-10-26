package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static final boolean	parseStack	= false;
	
	public static Dyvilc		instance;
	
	public CompilerConfig		config;
	public Set<CompilerState>	states		= new TreeSet();
	
	public static ParserManager	parser		= new ParserManager();
	public static boolean		debug;
	
	public Dyvilc(CompilerConfig config)
	{
		this.config = config;
	}
	
	public static void main(String[] args)
	{
		int i = 0;
		CodeFile file = new CodeFile(args[i++]);
		
		CompilerConfig config = new CompilerConfig();
		parser.parse(file, new ConfigParser(config));
		
		for (Library library : config.libraries)
		{
			library.loadLibrary();
		}
		
		Type.init();
		
		instance = new Dyvilc(config);
		for (int j = i; j < args.length; j++)
		{
			instance.addStates(args[j]);
		}
		
		instance.run();
	}
	
	public void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			this.states.add(CompilerState.TOKENIZE);
			this.states.add(CompilerState.PARSE);
			this.states.add(CompilerState.RESOLVE_TYPES);
			this.states.add(CompilerState.RESOLVE);
			this.states.add(CompilerState.OPERATOR_PRECEDENCE);
			this.states.add(CompilerState.FOLD_CONSTANTS);
			this.states.add(CompilerState.CONVERT);
			this.states.add(CompilerState.COMPILE);
			break;
		case "optimize":
			this.states.add(CompilerState.OPTIMIZE);
			break;
		case "obfuscate":
			this.states.add(CompilerState.OBFUSCATE);
			break;
		case "doc":
			this.states.add(CompilerState.DYVILDOC);
			break;
		case "decompile":
			this.states.add(CompilerState.DECOMPILE);
			break;
		case "debug":
			this.states.add(CompilerState.DEBUG);
			debug = true;
			break;
		}
	}
	
	public void run()
	{
		File sourceDir = this.config.sourceDir;
		File outputDir = this.config.outputDir;
		Package root = Package.rootPackage;
		System.out.println("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		System.out.println("Applying States " + this.states);
		System.out.println();
		
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
		
		for (CompilerState state : this.states)
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
