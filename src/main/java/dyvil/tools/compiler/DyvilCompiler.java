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
import dyvil.tools.compiler.util.Util;

public class DyvilCompiler
{
	public static boolean				parseStack;
	public static boolean				logFile	= true;
	public static boolean				debug;
	
	public static Logger				logger	= Logger.getLogger("DYVILC");
	public static DateFormat			format	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static CompilerConfig		config	= new CompilerConfig();
	public static Set<CompilerState>	states	= new TreeSet();
	
	public static ParserManager			parser	= new ParserManager();
	public static List<File>			files	= new ArrayList();
	
	public static void main(String[] args)
	{
		// Sets up States from arguments
		for (int i = 1; i < args.length; i++)
		{
			addStates(args[i]);
		}
		
		long now = System.nanoTime();
		
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
		
		File sourceDir = config.sourceDir;
		File outputDir = config.outputDir;
		Package root = Package.rootPackage;
		int states = DyvilCompiler.states.size();
		int libs = config.libraries.size();
		
		logger.info("Compiling " + sourceDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
		if (debug)
		{
			logger.info("Applying " + states + (states == 1 ? " State: " : " States: ") + DyvilCompiler.states);
		}
		
		Util.logProfile(now, libs, "Loaded " + libs + (libs == 1 ? " Library " : " Libraries ") + "(%.1f ms, %.1f ms/L, %.2f L/s)");
		
		now = System.nanoTime();
		
		// Scan for Packages and Compilation Units
		for (String s : sourceDir.list())
		{
			findUnits(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		int units = root.units.size();
		int packages = root.subPackages.size();
		logger.info("Compiling " + packages + (packages == 1 ? " Package, " : " Packages, ") + units + (units == 1 ? " Compilation Unit" : " Compilation Units"));
		logger.info("");
		
		// Apply states
		for (CompilerState state : DyvilCompiler.states)
		{
			CompilerState.applyState(state, root.units);
		}
		
		logger.info("");
		Util.logProfile(now, units, "Compilation finished (%.1f ms, %.1f ms/CU, %.2f CU/s)");
	}
	
	public static void initLogger()
	{
		try
		{
			logger.setUseParentHandlers(false);
			
			Formatter formatter = new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					String message = record.getMessage();
					if (message == null || message.isEmpty())
					{
						return "\n";
					}
					
					StringBuilder builder = new StringBuilder();
					builder.append('[').append(format.format(new Date(record.getMillis()))).append("] [");
					builder.append(record.getLevel()).append("]: ").append(message).append('\n');
					return builder.toString();
				}
			};
			StreamHandler ch = new StreamHandler(System.out, formatter);
			logger.addHandler(ch);
			
			if (logFile)
			{
				String path = new File("dyvilc.log").getAbsolutePath();
				FileHandler fh = new FileHandler(path, true);
				fh.setFormatter(formatter);
				logger.addHandler(fh);
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	public static void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			states.add(CompilerState.TOKENIZE);
			states.add(CompilerState.PARSE);
			states.add(CompilerState.RESOLVE_TYPES);
			states.add(CompilerState.RESOLVE);
			states.add(CompilerState.CHECK);
			// states.add(CompilerState.OPERATOR_PRECEDENCE);
			// states.add(CompilerState.CONVERT);
			states.add(CompilerState.COMPILE);
			break;
		case "optimize":
			states.add(CompilerState.FOLD_CONSTANTS);
			// states.add(CompilerState.OPTIMIZE);
			break;
		case "obfuscate":
			// states.add(CompilerState.OBFUSCATE);
			break;
		case "doc":
			// states.add(CompilerState.DYVILDOC);
			break;
		case "decompile":
			// states.add(CompilerState.DECOMPILE);
			break;
		case "jar":
			states.add(CompilerState.JAR);
			break;
		case "--debug":
			states.add(CompilerState.DEBUG);
			debug = true;
			break;
		case "--pstack":
			parseStack = true;
			break;
		case "--nolog":
			logFile = false;
			break;
		}
	}
	
	public static void findUnits(File source, File output, Package pack)
	{
		if (!source.exists())
		{
			logger.warning(source.getPath() + " does not exist.");
			return;
		}
		else if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				findUnits(new CodeFile(source, s), new File(output, s), pack.createSubPackage(name));
			}
			return;
		}
		else
		{
			String fileName = source.getPath();
			if (!config.compileFile(fileName))
			{
				return;
			}
			
			if (fileName.endsWith("Thumbs.db") || fileName.endsWith(".DS_Store"))
			{
				return;
			}
			else if (fileName.endsWith(".dyvil"))
			{
				CompilationUnit unit = new CompilationUnit(pack, (CodeFile) source, output);
				output = unit.outputFile;
				pack.addCompilationUnit(unit);
			}
			files.add(output);
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
