package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

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
	
	public static Logger		logger;
	public static DateFormat	format		= new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	
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
		// Sets up the logger
		initLogger();
		
		CodeFile file = new CodeFile(args[0]);
		
		// Loads the config
		CompilerConfig config = new CompilerConfig();
		parser.parse(file, new ConfigParser(config));
		
		// Loads libraries
		for (Library library : config.libraries)
		{
			library.loadLibrary();
		}
		
		// Inits primitive data types
		Type.init();
		
		// Sets up States from config
		instance = new Dyvilc(config);
		for (int i = 1; i < args.length; i++)
		{
			instance.addStates(args[i]);
		}
		
		instance.run();
	}
	
	public static void initLogger()
	{
		try
		{
			logger = Logger.getLogger("DYVILC");
			logger.setUseParentHandlers(false);
			
			String path = new File("dyvilc.log").getAbsolutePath();
			Formatter formatter = new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					StringBuilder builder = new StringBuilder();
					builder.append('[').append(format.format(new Date(record.getMillis()))).append("] [");
					builder.append(record.getLevel()).append("]: ").append(record.getMessage()).append('\n');
					return builder.toString();
				}
			};
			FileHandler fh = new FileHandler(path);
			fh.setFormatter(formatter);
			StreamHandler ch = new StreamHandler(System.out, formatter);
			
			logger.addHandler(fh);
			logger.addHandler(ch);
		}
		catch (Exception ex)
		{}
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
		
		logger.info("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		logger.info("Applying States " + this.states);
		logger.info("");
		
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
			logger.warning(source.getPath() + " does not exist.");
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
