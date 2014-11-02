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
import dyvil.tools.compiler.util.ParserUtil;

public class Dyvilc
{
	public static boolean				parseStack;
	public static boolean				debug;
	
	public static Logger				logger = Logger.getLogger("DYVILC");
	public static DateFormat			format	= new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	
	public static CompilerConfig		config	= new CompilerConfig();
	public static Set<CompilerState>	states	= new TreeSet();
	
	public static ParserManager			parser	= new ParserManager();
	
	public static void main(String[] args)
	{
		// Sets up the logger
		initLogger();
		
		// Loads the config
		parser.parse(new CodeFile(args[0]), new ConfigParser(config));
		
		// Loads libraries
		for (Library library : config.libraries)
		{
			library.loadLibrary();
		}
		
		// Inits primitive data types
		Type.init();
		
		// Sets up States from config
		for (int i = 1; i < args.length; i++)
		{
			addStates(args[i]);
		}
		
		run();
	}
	
	public static void initLogger()
	{
		try
		{
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
	
	public static void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			Dyvilc.states.add(CompilerState.TOKENIZE);
			Dyvilc.states.add(CompilerState.PARSE);
			Dyvilc.states.add(CompilerState.RESOLVE_TYPES);
			Dyvilc.states.add(CompilerState.RESOLVE);
			Dyvilc.states.add(CompilerState.OPERATOR_PRECEDENCE);
			Dyvilc.states.add(CompilerState.FOLD_CONSTANTS);
			Dyvilc.states.add(CompilerState.CONVERT);
			Dyvilc.states.add(CompilerState.COMPILE);
			break;
		case "optimize":
			Dyvilc.states.add(CompilerState.OPTIMIZE);
			break;
		case "obfuscate":
			Dyvilc.states.add(CompilerState.OBFUSCATE);
			break;
		case "doc":
			Dyvilc.states.add(CompilerState.DYVILDOC);
			break;
		case "decompile":
			Dyvilc.states.add(CompilerState.DECOMPILE);
			break;
		case "--debug":
			Dyvilc.states.add(CompilerState.DEBUG);
			debug = true;
			break;
		case "--pstack":
			parseStack = true;
			break;
		}
	}
	
	public static void run()
	{
		long now = System.nanoTime();
		
		File sourceDir = Dyvilc.config.sourceDir;
		File outputDir = Dyvilc.config.outputDir;
		Package root = Package.rootPackage;
		int states = Dyvilc.states.size();
		
		logger.info("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		if (debug)
		{
			logger.info("Applying " + states + " States: " + Dyvilc.states);
			logger.info("");
		}
		
		for (String s : sourceDir.list())
		{
			compile(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		List<CompilationUnit> units = new ArrayList(root.subPackages.size());
		for (Package pack : root.subPackages)
		{
			for (CompilationUnit unit : pack.units)
			{
				units.add(unit);
			}
		}
		
		for (CompilerState state : Dyvilc.states)
		{
			state.apply(units, null);
		}
		
		logger.info("");
		ParserUtil.logProfile(now, units.size(), "Compilation finished (%.1f ms, %.1f ms/CU, %.2f CU/s)");
	}
	
	public static void compile(File source, File output, Package pack)
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
				compile(new CodeFile(source, s), new File(output, s), pack.createSubPackage(name));
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
