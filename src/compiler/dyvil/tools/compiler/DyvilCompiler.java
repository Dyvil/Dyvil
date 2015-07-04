package dyvil.tools.compiler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.*;

import dyvil.io.AppendableOutputStream;
import dyvil.io.FileUtils;
import dyvil.io.LoggerOutputStream;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.config.ConfigParser;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.phase.ICompilerPhase;
import dyvil.tools.compiler.sources.FileFinder;
import dyvil.tools.compiler.util.TestThread;
import dyvil.tools.compiler.util.Util;

import org.objectweb.asm.Opcodes;

public final class DyvilCompiler
{
	public static final String			VERSION				= "1.0.0";
	public static final String			DYVIL_VERSION		= "1.0.0";
	
	public static boolean				parseStack;
	public static String				logFile;
	public static boolean				debug;
	public static int					constantFolding;
	
	public static int					classVersion		= Opcodes.V1_8;
	public static int					asmVersion			= Opcodes.ASM5;
	public static int					maxConstantDepth	= 10;
	
	public static Logger				logger				= Logger.getLogger("DYVILC");
	public static LoggerOutputStream	loggerOut			= new LoggerOutputStream(logger, "TEST-OUT");
	public static LoggerOutputStream	loggerErr			= new LoggerOutputStream(logger, "TEST-ERR");
	public static DateFormat			format				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static CompilerConfig		config				= new CompilerConfig();
	public static Set<ICompilerPhase>	phases				= new TreeSet();
	public static FileFinder			fileFinder			= new FileFinder();
	
	public static void main(String[] args)
	{
		long now = System.nanoTime();
		long totalTime = now;
		
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
		fileFinder.findUnits(sourceDir, outputDir, null);
		
		int fileCount = fileFinder.files.size();
		int unitCount = fileFinder.units.size();
		int packages = Package.rootPackage.subPackages.size();
		now = System.nanoTime() - now;
		logger.info("Found " + packages + (packages == 1 ? " Package, " : " Packages, ") + fileCount + (fileCount == 1 ? " File (" : " Files (") + unitCount
				+ (unitCount == 1 ? " Compilation Unit)" : " Compilation Units)") + " (" + Util.toTime(now) + ")");
		logger.info("");
		
		now = System.nanoTime();
		
		// Apply states
		if (debug)
		{
			logger.info("Applying " + phases + (phases == 1 ? " Phase: " : " Phases: ") + DyvilCompiler.phases);
			for (ICompilerPhase phase : DyvilCompiler.phases)
			{
				long now1 = System.nanoTime();
				logger.info("Applying " + phase.getName());
				try
				{
					phase.apply(fileFinder.units);
					now1 = System.nanoTime() - now1;
					logger.info(phase.getName() + " completed (" + Util.toTime(now1) + ")");
				}
				catch (Throwable t)
				{
					logger.info(phase.getName() + " failed!");
					logger.throwing(phase.getName(), "apply", t);
					logger.info("");
					logger.info("Compilation FAILED (" + Util.toTime(System.nanoTime() - now) + ")");
					return;
				}
			}
		}
		else
		{
			for (ICompilerPhase phase : DyvilCompiler.phases)
			{
				try
				{
					phase.apply(fileFinder.units);
				}
				catch (Throwable t)
				{
					logger.throwing(phase.getName(), "apply", t);
					logger.info("");
					logger.info("Compilation FAILED (" + Util.toTime(System.nanoTime() - now) + ")");
					return;
				}
			}
		}
		
		long l = System.nanoTime();
		
		logger.info("");
		logger.info("Compilation finished (" + Util.toTime(l - now) + ", Total Running Time: " + Util.toTime(l - totalTime) + ")");
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
			phases.add(ICompilerPhase.CLEANUP);
			return;
			// case "obfuscate":
			// case "doc":
			// case "decompile":
		case "optimize":
			phases.add(ICompilerPhase.FOLD_CONSTANTS);
			constantFolding = 1;
			return;
		case "jar":
			phases.add(ICompilerPhase.CLEAN);
			phases.add(ICompilerPhase.JAR);
			return;
		case "format":
			phases.add(ICompilerPhase.TOKENIZE);
			phases.add(ICompilerPhase.PARSE);
			phases.add(ICompilerPhase.FORMAT);
			return;
		case "clean":
			phases.add(ICompilerPhase.CLEAN);
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
	
	public static void test()
	{
		String mainType = config.mainType;
		if (mainType == null)
		{
			return;
		}
		
		File file = new File(config.outputDir, config.mainType.replace('.', '/') + ".class");
		if (!file.exists())
		{
			DyvilCompiler.logger.info("The Main Type '" + config.mainType + "' does not exist or was not compiled, skipping test.");
			return;
		}
		
		new TestThread().start();
	}
	
	public static void clean()
	{
		File[] files = config.outputDir.listFiles();
		if (files == null)
		{
			return;
		}
		
		for (File s : files)
		{
			FileUtils.delete(s);
		}
	}
}
