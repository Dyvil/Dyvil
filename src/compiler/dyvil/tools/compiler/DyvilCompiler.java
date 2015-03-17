package dyvil.tools.compiler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import org.objectweb.asm.Opcodes;

import dyvil.io.AppendableOutputStream;
import dyvil.io.LoggerOutputStream;
import dyvil.tools.compiler.ast.dwt.DWTFile;
import dyvil.tools.compiler.ast.structure.DyvilFile;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.config.ConfigParser;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.phase.ICompilerPhase;
import dyvil.tools.compiler.util.Util;

public final class DyvilCompiler
{
	public static final String				VERSION				= "1.0.0";
	public static final String				DYVIL_VERSION		= "1.0.0";
	
	public static boolean					parseStack;
	public static boolean					logFile				= true;
	public static boolean					debug;
	public static int						constantFolding;
	
	public static int						classVersion		= Opcodes.V1_8;
	public static int						asmVersion			= Opcodes.ASM5;
	public static int						maxConstantDepth	= 10;
	
	public static Logger					logger				= Logger.getLogger("DYVILC");
	public static LoggerOutputStream		loggerOut			= new LoggerOutputStream(logger, "TEST-OUT");
	public static LoggerOutputStream		loggerErr			= new LoggerOutputStream(logger, "TEST-ERR");
	public static DateFormat				format				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static CompilerConfig			config				= new CompilerConfig();
	public static Set<ICompilerPhase>		states				= new TreeSet();
	
	public static List<File>				files				= new ArrayList();
	public static List<ICompilationUnit>	units				= new ArrayList();
	
	public static void main(String[] args)
	{
		long now = System.nanoTime();
		
		// Sets up States from arguments
		for (int i = 1; i < args.length; i++)
		{
			addStates(args[i]);
		}
		
		// Sets up the logger
		initLogger();
		
		// Loads the config
		loadConfig(args[0]);
		
		File sourceDir = config.sourceDir;
		File outputDir = config.outputDir;
		Package root = Package.rootPackage;
		int states = DyvilCompiler.states.size();
		int libs = config.libraries.size();
		
		logger.info("Dyvil Compiler " + VERSION + " for Dyvil " + DYVIL_VERSION);
		logger.info("");
		
		if (debug)
		{
			logger.fine("Startup Time: " + (System.nanoTime() - now) / 1000000L + " ms");
		}
		
		if (DyvilCompiler.states.contains(ICompilerPhase.RESOLVE_TYPES))
		{
			now = System.nanoTime();
			
			// Loads libraries
			for (Library library : config.libraries)
			{
				library.loadLibrary();
			}
			
			// Inits primitive data types
			Package.init();
			Type.init();
			
			now = System.nanoTime() - now;
			logger.info("Loaded " + libs + (libs == 1 ? " Library " : " Libraries ") + "(" + Util.toTime(now) + ")");
		}
		
		now = System.nanoTime();
		logger.info("Compiling '" + sourceDir + "' to '" + outputDir + "'");
		if (debug)
		{
			logger.info("Applying " + states + (states == 1 ? " State: " : " States: ") + DyvilCompiler.states);
		}
		
		// Scan for Packages and Compilation Units
		for (String s : sourceDir.list())
		{
			findUnits(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		int fileCount = files.size();
		int unitCount = units.size();
		int packages = root.subPackages.size();
		logger.info("Compiling " + packages + (packages == 1 ? " Package, " : " Packages, ") + fileCount + (fileCount == 1 ? " File (" : " Files (")
				+ unitCount + (unitCount == 1 ? " Compilation Unit)" : " Compilation Units)"));
		logger.info("");
		
		// Apply states
		if (debug)
		{
			for (ICompilerPhase state : DyvilCompiler.states)
			{
				logger.info("Applying " + state.getName());
				long now1 = System.nanoTime();
				state.apply(units);
				now1 = System.nanoTime() - now1;
				logger.info("Completed " + state.getName() + " (" + Util.toTime(now1) + ")");
			}
		}
		else
		{
			for (ICompilerPhase state : DyvilCompiler.states)
			{
				state.apply(units);
			}
		}
		
		now = System.nanoTime() - now;
		logger.info("");
		logger.info("Compilation finished (" + Util.toTime(now) + ")");
	}
	
	private static void initLogger()
	{
		try
		{
			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
			
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
					
					Throwable thrown = record.getThrown();
					StringBuilder builder = new StringBuilder();
					
					if (DyvilCompiler.debug)
					{
						builder.append('[').append(format.format(new Date(record.getMillis()))).append("] [");
						builder.append(record.getLevel()).append("]: ");
					}
					builder.append(message).append('\n');
					
					if (thrown != null)
					{
						thrown.printStackTrace(new AppendableOutputStream(builder));
					}
					return builder.toString();
				}
			};
			StreamHandler ch = new StreamHandler(System.out, formatter);
			ch.setLevel(Level.ALL);
			logger.addHandler(ch);
			
			if (logFile)
			{
				String path = new File("dyvilc.log").getAbsolutePath();
				FileHandler fh = new FileHandler(path, true);
				fh.setLevel(Level.ALL);
				fh.setFormatter(formatter);
				logger.addHandler(fh);
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	private static void loadConfig(String source)
	{
		CodeFile file = new CodeFile(source);
		TokenIterator tokens = Dlex.tokenIterator(file.getCode());
		new ParserManager(new ConfigParser(config)).parse(null, tokens);
	}
	
	private static void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			states.add(ICompilerPhase.TOKENIZE);
			states.add(ICompilerPhase.PARSE);
			states.add(ICompilerPhase.RESOLVE_TYPES);
			states.add(ICompilerPhase.RESOLVE);
			states.add(ICompilerPhase.CHECK_TYPES);
			states.add(ICompilerPhase.CHECK);
			states.add(ICompilerPhase.COMPILE);
			return;
			// case "obfuscate":
			// case "doc":
			// case "decompile":
		case "optimize":
			states.add(ICompilerPhase.FOLD_CONSTANTS);
			constantFolding = 1;
			return;
		case "jar":
			states.add(ICompilerPhase.JAR);
			return;
		case "format":
			states.add(ICompilerPhase.TOKENIZE);
			states.add(ICompilerPhase.PARSE);
			states.add(ICompilerPhase.FORMAT);
			return;
		case "print":
			states.add(ICompilerPhase.PRINT);
			return;
		case "test":
			states.add(ICompilerPhase.TEST);
			return;
		case "--debug":
			states.add(ICompilerPhase.PRINT);
			states.add(ICompilerPhase.TEST);
			debug = true;
			return;
		case "--pstack":
			parseStack = true;
			return;
		case "--nolog":
			logFile = false;
			return;
		}
		
		if (s.startsWith("-o"))
		{
			try
			{
				states.add(ICompilerPhase.FOLD_CONSTANTS);
				constantFolding = Integer.parseInt(s.substring(2));
				return;
			}
			catch (Exception ex)
			{
			}
		}
		
		System.out.println("Invalid Argument '" + s + "'. Ignoring.");
	}
	
	private static void findUnits(File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				findUnits(new CodeFile(source, s), new File(output, s), pack.createSubPackage(name));
			}
			return;
		}
		
		String fileName = source.getPath();
		if (!config.compileFile(fileName))
		{
			return;
		}
		
		if (fileName.endsWith("Thumbs.db") || fileName.endsWith(".DS_Store"))
		{
			return;
		}
		else if (fileName.endsWith(".dwt"))
		{
			DWTFile dwt = new DWTFile(pack, (CodeFile) source, output);
			output = dwt.outputFile;
			units.add(dwt);
		}
		else if (fileName.endsWith(".dyvil"))
		{
			DyvilFile unit = new DyvilFile(pack, (CodeFile) source, output);
			output = unit.outputFile;
			pack.addCompilationUnit(unit);
			units.add(unit);
		}
		files.add(output);
	}
}
