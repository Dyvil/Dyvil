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
import dyvil.tools.compiler.ast.structure.DyvilUnit;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.config.ConfigParser;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.phase.ICompilerPhase;
import dyvil.tools.compiler.util.Util;

public final class DyvilCompiler
{
	public static final String				VERSION				= "1.0.0";
	public static final String				DYVIL_VERSION		= "1.0.0";
	
	public static boolean					parseStack;
	public static String					logFile;
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
	public static Set<ICompilerPhase>		phases				= new TreeSet();
	
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
		if (!sourceDir.exists())
		{
			logger.severe("The specified source path '" + sourceDir + "' does not exist. Skipping Compilation.");
		}
		
		File outputDir = config.outputDir;
		int phases = DyvilCompiler.phases.size();
		int libs = config.libraries.size();
		
		logger.info("Dyvil Compiler " + VERSION + " for Dyvil " + DYVIL_VERSION);
		logger.info("");
		
		logger.fine("Loaded Config (" + Util.toTime(System.nanoTime() - now) + ")");
		
		if (DyvilCompiler.phases.contains(ICompilerPhase.RESOLVE_TYPES))
		{
			now = System.nanoTime();
			
			// Loads libraries
			for (Library library : config.libraries)
			{
				library.loadLibrary();
			}
			
			long now1 = System.nanoTime();
			now = now1 - now;
			logger.fine("Loaded " + libs + (libs == 1 ? " Library (" : " Libraries (") + Util.toTime(now) + ")");
			
			// Inits primitive data types
			Package.init();
			Types.init();
			
			now1 = System.nanoTime() - now1;
			logger.fine("Loaded Base Types (" + Util.toTime(now1) + ")");
		}
		
		now = System.nanoTime();
		logger.info("Compiling '" + sourceDir + "' to '" + outputDir + "'");
		
		// Scan for Packages and Compilation Units
		findUnits(sourceDir, outputDir, null);
		
		int fileCount = files.size();
		int unitCount = units.size();
		int packages = Package.rootPackage.subPackages.size();
		logger.info("Compiling " + packages + (packages == 1 ? " Package, " : " Packages, ") + fileCount + (fileCount == 1 ? " File (" : " Files (")
				+ unitCount + (unitCount == 1 ? " Compilation Unit)" : " Compilation Units)"));
		logger.info("");
		
		// Apply states
		if (debug)
		{
			logger.info("Applying " + phases + (phases == 1 ? " Phase: " : " Phases: ") + DyvilCompiler.phases);
			for (ICompilerPhase phase : DyvilCompiler.phases)
			{
				long now1 = System.nanoTime();
				logger.info("Applying " + phase.getName());
				phase.apply(units);
				now1 = System.nanoTime() - now1;
				logger.info("Completed " + phase.getName() + " (" + Util.toTime(now1) + ")");
			}
		}
		else
		{
			for (ICompilerPhase phase : DyvilCompiler.phases)
			{
				phase.apply(units);
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
			
			if (logFile != null)
			{
				FileHandler fh = new FileHandler(logFile, true);
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
		ConfigParser.parse(file.getCode(), config);
	}
	
	private static void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			phases.add(ICompilerPhase.TOKENIZE);
			phases.add(ICompilerPhase.PARSE);
			phases.add(ICompilerPhase.RESOLVE_TYPES);
			phases.add(ICompilerPhase.RESOLVE);
			phases.add(ICompilerPhase.CHECK_TYPES);
			phases.add(ICompilerPhase.CHECK);
			phases.add(ICompilerPhase.COMPILE);
			return;
			// case "obfuscate":
			// case "doc":
			// case "decompile":
		case "optimize":
			phases.add(ICompilerPhase.FOLD_CONSTANTS);
			constantFolding = 1;
			return;
		case "jar":
			phases.add(ICompilerPhase.JAR);
			return;
		case "format":
			phases.add(ICompilerPhase.TOKENIZE);
			phases.add(ICompilerPhase.PARSE);
			phases.add(ICompilerPhase.FORMAT);
			return;
		case "print":
			phases.add(ICompilerPhase.PRINT);
			return;
		case "test":
			phases.add(ICompilerPhase.TEST);
			return;
		case "--debug":
			phases.add(ICompilerPhase.PRINT);
			phases.add(ICompilerPhase.TEST);
			debug = true;
			return;
		case "--pstack":
			parseStack = true;
			return;
		}
		
		if (s.startsWith("--logFile="))
		{
			logFile = s.substring(10);
			return;
		}
		if (s.startsWith("-o"))
		{
			try
			{
				phases.add(ICompilerPhase.FOLD_CONSTANTS);
				constantFolding = Integer.parseInt(s.substring(2));
				return;
			}
			catch (Exception ex)
			{
			}
		}
		
		System.err.println("Invalid Argument '" + s + "'. Ignoring.");
	}
	
	private static void findUnits(File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				findUnits(new CodeFile(source, s), new File(output, s), pack == null ? Package.rootPackage : pack.createSubPackage(name));
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
		files.add(output);
		if (fileName.endsWith(".dwt"))
		{
			DWTFile dwt = new DWTFile(pack, (CodeFile) source, output);
			units.add(dwt);
			return;
		}
		if (fileName.endsWith(".dyvil") || fileName.endsWith(".dyv"))
		{
			DyvilUnit unit = new DyvilUnit(pack, (CodeFile) source, output);
			pack.addCompilationUnit(unit);
			units.add(unit);
			return;
		}
		if (fileName.endsWith(".dyh"))
		{
			DyvilHeader header = new DyvilHeader(pack, (CodeFile) source, output);
			pack.addCompilationUnit(header);
			units.add(0, header);
			return;
		}
	}
}
