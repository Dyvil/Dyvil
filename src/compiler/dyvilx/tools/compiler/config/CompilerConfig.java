package dyvilx.tools.compiler.config;

import dyvil.io.Files;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.phase.ICompilerPhase;
import dyvilx.tools.parsing.marker.MarkerStyle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CompilerConfig
{
	// =============== Constants ===============

	public static final String DEFAULT_JAR_FILE_FORMAT = "%1$s-%2$s.jar";

	public static final int DEFAULT_CONSTANT_FOLDING   = 2;
	public static final int OPTIMIZE_CONSTANT_FOLDING  = 5;
	public static final int DEFAULT_MAX_CONSTANT_DEPTH = 10;

	// =============== Fields ===============

	private final DyvilCompiler compiler;

	// --------------- Config Values ---------------

	// - - - - - - - - Debug - - - - - - - -

	private boolean     debug;
	private boolean     ansiColors;
	private MarkerStyle markerStyle = MarkerStyle.DYVIL;

	// - - - - - - - - Sources - - - - - - - -

	public final List<File> sourceDirs = new ArrayList<>();

	public final List<Pattern> includePatterns = new ArrayList<>();
	public final List<Pattern> excludePatterns = new ArrayList<>();

	// - - - - - - - - Libraries - - - - - - - -

	public final List<Library> libraries = new ArrayList<>();

	// - - - - - - - - Compilation - - - - - - - -

	private int constantFolding = DEFAULT_CONSTANT_FOLDING;

	private int maxConstantDepth = DEFAULT_MAX_CONSTANT_DEPTH;

	// - - - - - - - - Output - - - - - - - -

	private File outputDir;

	// - - - - - - - - Test - - - - - - - -

	private String mainType;

	public final List<String> mainArgs = new ArrayList<>();

	private File testDir = new File(".");

	// =============== Constructors ===============

	public CompilerConfig(DyvilCompiler compiler)
	{
		this.compiler = compiler;
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

	// - - - - - - - - Debug - - - - - - - -

	public boolean isDebug()
	{
		return this.debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public boolean useAnsiColors()
	{
		return this.ansiColors;
	}

	public void setAnsiColors(boolean ansiColors)
	{
		this.ansiColors = ansiColors;
	}

	public MarkerStyle getMarkerStyle()
	{
		return this.markerStyle;
	}

	public void setMarkerStyle(MarkerStyle markerStyle)
	{
		this.markerStyle = markerStyle;
	}

	// - - - - - - - - Sources - - - - - - - -

	public List<File> getSourceDirs()
	{
		return this.sourceDirs;
	}

	public List<Pattern> getIncludePatterns()
	{
		return this.includePatterns;
	}

	public List<Pattern> getExcludePatterns()
	{
		return this.excludePatterns;
	}

	public void include(String pattern)
	{
		this.includePatterns.add(Files.antPattern(pattern));
	}

	public void exclude(String pattern)
	{
		this.excludePatterns.add(Files.antPattern(pattern));
	}

	public boolean isIncluded(String name)
	{
		return this.checkIsIncluded(name) && this.checkNotExcluded(name);
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean checkIsIncluded(String name)
	{
		// If no include patterns are specified, then all files will be included.
		if (this.includePatterns.isEmpty())
		{
			return true;
		}
		// If any include patterns are specified, then a file is included if it matches any of the patterns.
		return this.includePatterns.stream().anyMatch(p -> p.matcher(name).find());
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean checkNotExcluded(String name)
	{
		// If no exclude patterns are specified, then no files will be excluded.
		if (this.excludePatterns.isEmpty())
		{
			return true;
		}
		// If any exclude patterns are specified, then a file is included only if it matches none of the patterns.
		return this.excludePatterns.stream().noneMatch(p -> p.matcher(name).find());
	}

	// - - - - - - - - Libraries - - - - - - - -

	public List<Library> getLibraries()
	{
		return this.libraries;
	}

	public void loadLibrary(String file)
	{
		final Library load = Library.load(new File(file));
		if (load != null)
		{
			this.libraries.add(load);
		}
		else
		{
			this.compiler.warn(I18n.get("library.not_found", file));
		}
	}

	// - - - - - - - - Compilation - - - - - - - -

	public int getConstantFolding()
	{
		return this.constantFolding;
	}

	public void setConstantFolding(int constantFolding)
	{
		this.constantFolding = constantFolding;
	}

	public int getMaxConstantDepth()
	{
		return this.maxConstantDepth;
	}

	public void setMaxConstantDepth(int maxConstantDepth)
	{
		this.maxConstantDepth = maxConstantDepth;
	}

	// - - - - - - - - Output - - - - - - - -

	public File getOutputDir()
	{
		return this.outputDir;
	}

	public void setOutputDir(File outputDir)
	{
		this.outputDir = outputDir;
	}

	// - - - - - - - - Test - - - - - - - -

	public String getMainType()
	{
		return this.mainType;
	}

	public void setMainType(String mainType)
	{
		this.mainType = mainType;
	}

	public List<String> getMainArgs()
	{
		return this.mainArgs;
	}

	public File getTestDir()
	{
		return this.testDir;
	}

	public void setTestDir(File testDir)
	{
		this.testDir = testDir;
	}

	// --------------- Dynamic Property Parsing ---------------

	public void addOptions(Options options)
	{
		options.addOption("D", "debug", false, "enables debug output");
		options.addOption("A", "ansi", false, "enables colored output using ANSI color codes");
		options.addOption("M", "marker-style", true,
		                  "sets the output style of diagnostic markers. supported values: dyvil, gcc, machine (case-insensitive)");
		options.addOption("C", "max-constant-depth", true,
		                  "sets the maximum constant depth for constant expression resolution. can be any non-negative integer");
		options.addOption("F", "max-constant-folding", true,
		                  "sets the maximum constant folding depth. can be any non-negative integer, 0 = no constant folding");
		// dump-dir is used by REPL
		// g/gensrc-dir are used by GenSrc

		final Option sourceDirs = new Option("s", "source-dirs", true,
		                                     "source directories, separated by '" + File.pathSeparatorChar + "'");
		sourceDirs.setArgs(Option.UNLIMITED_VALUES);
		sourceDirs.setValueSeparator(File.pathSeparatorChar);
		options.addOption(sourceDirs);

		final Option includes = new Option("i", "include-patterns", true,
		                                   "ant-style include patterns, separated by '" + File.pathSeparatorChar + "'");
		includes.setArgs(Option.UNLIMITED_VALUES);
		includes.setValueSeparator(File.pathSeparatorChar);
		options.addOption(includes);

		final Option excludes = new Option("x", "exclude-patterns", true,
		                                   "ant-style exclude patterns, separated by '" + File.pathSeparatorChar + "'");
		excludes.setArgs(Option.UNLIMITED_VALUES);
		excludes.setValueSeparator(File.pathSeparatorChar);
		options.addOption(excludes);

		final Option classpath = new Option("cp", "classpath", true,
		                                    "classpath libraries, separated by '" + File.pathSeparatorChar + "'");
		classpath.setArgs(Option.UNLIMITED_VALUES);
		classpath.setValueSeparator(File.pathSeparatorChar);
		options.addOption(classpath);

		options.addOption(null, "output-dir", true, "the target directory for generated bytecode");

		// Test Options

		options.addOption(null, "main-type", true, "the main class to invoke during the test phase");

		final Option mainArgs = new Option(null, "main-args", true,
		                                   "the main arguments for the test phase, separated by ' ' (space)");
		mainArgs.setArgs(Option.UNLIMITED_VALUES);
		mainArgs.setValueSeparator(' ');
		options.addOption(mainArgs);

		options.addOption(null, "test-dir", true, "the working directory to run the test phase in");
	}

	public void readOptions(CommandLine cmd)
	{
		if (cmd.hasOption("debug"))
		{
			this.setDebug(true);
			this.compiler.phases.add(ICompilerPhase.PRINT); // print after parse
			this.compiler.phases.add(ICompilerPhase.TEST);
		}

		this.setAnsiColors(cmd.hasOption("ansi"));

		if (cmd.hasOption("max-constant-folding"))
		{
			final String level = cmd.getOptionValue("max-constant-folding");
			try
			{
				this.setConstantFolding(Integer.parseUnsignedInt(level));
				this.compiler.phases.add(ICompilerPhase.FOLD_CONSTANTS);
			}
			catch (Exception ignored)
			{
				this.compiler.warn(I18n.get("option.max-constant-folding.invalid", level));
			}
		}

		if (cmd.hasOption("max-constant-depth"))
		{
			final String level = cmd.getOptionValue("max-constant-depth");
			try
			{
				this.setMaxConstantDepth(Integer.parseUnsignedInt(level));
			}
			catch (Exception ignored)
			{
				this.compiler.warn(I18n.get("option.max-constant-depth.invalid", level));
			}
		}

		if (cmd.hasOption("marker-style"))
		{
			final String optionValue = cmd.getOptionValue("marker-style");

			try
			{
				this.setMarkerStyle(MarkerStyle.valueOf(optionValue.toUpperCase()));
			}
			catch (IllegalArgumentException ex)
			{
				this.compiler.warn(I18n.get("option.marker-style.unknown", optionValue));
			}
		}

		if (cmd.hasOption("source-dirs"))
		{
			for (final String sourceDir : cmd.getOptionValues("source-dirs"))
			{
				this.sourceDirs.add(new File(sourceDir));
			}
		}

		if (cmd.hasOption("include-patterns"))
		{
			for (final String pattern : cmd.getOptionValues("include-patterns"))
			{
				this.includePatterns.add(Files.antPattern(pattern));
			}
		}

		if (cmd.hasOption("exclude-patterns"))
		{
			for (final String pattern : cmd.getOptionValues("exclude-patterns"))
			{
				this.excludePatterns.add(Files.antPattern(pattern));
			}
		}

		if (cmd.hasOption("classpath"))
		{
			for (final String entry : cmd.getOptionValues("classpath"))
			{
				this.loadLibrary(entry);
			}
		}

		if (cmd.hasOption("output-dir"))
		{
			this.setOutputDir(new File(cmd.getOptionValue("output-dir")));
		}

		// Test Options
		// TODO deprecated, remove in v0.47.0

		if (cmd.hasOption("main-type"))
		{
			this.compiler.warn(I18n.get("option.deprecated", "main-type", "0.47.0"));
			this.setMainType(cmd.getOptionValue("main-type"));
		}

		if (cmd.hasOption("main-args"))
		{
			this.compiler.warn(I18n.get("option.deprecated", "main-args", "0.47.0"));
			Collections.addAll(this.mainArgs, cmd.getOptionValues("main-args"));
		}

		if (cmd.hasOption("test-dir"))
		{
			this.compiler.warn(I18n.get("option.deprecated", "test-dir", "0.47.0"));
			this.setTestDir(new File(cmd.getOptionValue("test-dir")));
		}
	}
}
