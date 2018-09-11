package dyvilx.tools.compiler.config;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.FileUtils;
import dyvil.lang.Strings;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;

import java.io.File;
import java.io.FileNotFoundException;
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

	private File logFile;

	private boolean debug;
	private boolean ansiColors;

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

	// - - - - - - - - Jar - - - - - - - -

	private String jarName;
	private String jarVendor;
	private String jarVersion;
	private String jarNameFormat = DEFAULT_JAR_FILE_FORMAT;

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

	public File getLogFile()
	{
		return this.logFile;
	}

	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}

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
		this.includePatterns.add(FileUtils.antToRegex(pattern));
	}

	public void exclude(String pattern)
	{
		this.excludePatterns.add(FileUtils.antToRegex(pattern));
	}

	public boolean isIncluded(String name)
	{
		// All match
		for (Pattern p : this.excludePatterns)
		{
			if (p.matcher(name).find())
			{
				return false;
			}
		}

		if (this.includePatterns.isEmpty())
		{
			return true;
		}

		// Any match
		for (Pattern p : this.includePatterns)
		{
			if (p.matcher(name).find())
			{
				return true;
			}
		}

		return false;
	}

	// - - - - - - - - Libraries - - - - - - - -

	public List<Library> getLibraries()
	{
		return this.libraries;
	}

	public void loadLibrary(String file)
	{
		try
		{
			this.libraries.add(Library.load(new File(file)));
		}
		catch (FileNotFoundException ex)
		{
			this.compiler.error(I18n.get("library.not_found", file), ex);
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

	// - - - - - - - - Jar - - - - - - - -

	public String getJarFileName()
	{
		return String.format(this.jarNameFormat, this.jarName, this.jarVersion);
	}

	public String getJarName()
	{
		return this.jarName;
	}

	public void setJarName(String jarName)
	{
		this.jarName = jarName;
	}

	public String getJarVendor()
	{
		return this.jarVendor;
	}

	public void setJarVendor(String jarVendor)
	{
		this.jarVendor = jarVendor;
	}

	public String getJarVersion()
	{
		return this.jarVersion;
	}

	public void setJarVersion(String jarVersion)
	{
		this.jarVersion = jarVersion;
	}

	public String getJarNameFormat()
	{
		return this.jarNameFormat;
	}

	public void setJarNameFormat(String jarNameFormat)
	{
		this.jarNameFormat = jarNameFormat;
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

	public boolean addProperty(String name, String value)
	{
		switch (name)
		{
		case "source_dirs":
			this.sourceDirs.add(new File(value));
			return true;
		case "main_args":
			this.mainArgs.add(value);
			return true;
		case "includes":
		case "include_patterns":
			this.include(value);
			return true;
		case "excludes":
		case "exclude_patterns":
			this.exclude(value);
			return true;
		case "libraries":
			this.loadLibrary(value);
			return true;
		}
		return false;
	}

	public boolean setProperty(String name, String value)
	{
		switch (name)
		{
		case "jar_name":
			this.setJarName(value);
			return true;
		case "jar_vendor":
			this.setJarVendor(value);
			return true;
		case "jar_version":
			this.setJarVersion(value);
			return true;
		case "jar_format":
			this.setJarNameFormat(value);
			return true;
		case "log_file":
			this.setLogFile(new File(value));
			return true;
		case "source_dir":
			this.sourceDirs.clear();
			this.sourceDirs.add(new File(value));
			return true;
		case "source_dirs":
			this.sourceDirs.clear();
			for (String path : Strings.split(value, ':'))
			{
				this.sourceDirs.add(new File(path));
			}
			return true;
		case "output_dir":
			this.setOutputDir(new File(value));
			return true;
		case "main_type":
			this.setMainType(value);
			return true;
		case "main_args": // deprecated
			this.mainArgs.add(value);
			return true;
		case "test_dir":
			this.setTestDir(new File(value));
			return true;
		case "include_patterns":
			this.includePatterns.clear();
			for (String pattern : Strings.split(value, ':'))
			{
				this.include(pattern);
			}
			return true;
		case "exclude":
			this.excludePatterns.clear();
			for (String pattern : Strings.split(value, ':'))
			{
				this.exclude(pattern);
			}
			return true;
		case "libraries":
			this.libraries.clear();
			for (String path : Strings.split(value, ':'))
			{
				this.loadLibrary(path);
			}
			return true;
		}
		return false;
	}
}
