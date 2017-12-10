package dyvilx.tools.compiler.config;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.FileUtils;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.sources.FileFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public class CompilerConfig
{
	private DyvilCompiler compiler;

	private String jarName;
	private String jarVendor;
	private String jarVersion;
	private String jarNameFormat = "%1$s-%2$s.jar";

	private File logFile;

	public final List<File>    sourceDirs = new ArrayList<>();
	public final List<Pattern> include    = new ArrayList<>();
	public final List<Pattern> exclude    = new ArrayList<>();

	public final List<Library> libraries = new ArrayList<>();

	private File outputDir;

	private String mainType;
	public final List<String> mainArgs = new ArrayList<>();
	private      File         testDir  = new File(".");

	private boolean debug;
	private boolean ansiColors;

	private int constantFolding = 2;

	private int maxConstantDepth = 10;

	public CompilerConfig(DyvilCompiler compiler)
	{
		this.compiler = compiler;
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

	public void setTestDir(String testDir)
	{
		this.testDir = new File(testDir);
	}

	public File getOutputDir()
	{
		return this.outputDir;
	}

	public void setOutputDir(File outputDir)
	{
		this.outputDir = outputDir;
	}

	public File getLogFile()
	{
		return this.logFile;
	}

	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}

	public void addLibraryFile(String file)
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

	public void includeFile(String pattern)
	{
		this.include.add(FileUtils.antToRegex(pattern));
	}

	public void excludeFile(String pattern)
	{
		this.exclude.add(FileUtils.antToRegex(pattern));
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

	public boolean isIncluded(String name)
	{
		// All match
		for (Pattern p : this.exclude)
		{
			if (p.matcher(name).find())
			{
				return false;
			}
		}

		if (this.include.isEmpty())
		{
			return true;
		}

		// Any match
		for (Pattern p : this.include)
		{
			if (p.matcher(name).find())
			{
				return true;
			}
		}

		return false;
	}

	public void findUnits(FileFinder fileFinder)
	{
		for (File sourceDir : this.sourceDirs)
		{
			fileFinder.process(this.compiler, sourceDir, this.outputDir, Package.rootPackage);
		}
	}

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
		case "include":
		case "includes":
			this.includeFile(value);
			return true;
		case "exclude":
		case "excludes":
			this.excludeFile(value);
			return true;
		case "libraries":
			this.addLibraryFile(value);
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
		case "source_dirs": // deprecated
			this.sourceDirs.add(new File(value));
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
			this.setTestDir(value);
			return true;
		case "include":
			this.includeFile(value);
			return true;
		case "exclude":
			this.excludeFile(value);
			return true;
		case "libraries": // deprecated
			this.addLibraryFile(value);
			return true;
		}
		return false;
	}

	public String getJarName()
	{
		return String.format(this.jarNameFormat, this.jarName, this.jarVersion);
	}

	@Override
	public String toString()
	{
		return "CompilerConfig(jarName: " + this.getJarName() + ", sourceDirs: " + this.sourceDirs + ", outputDir: "
		       + this.outputDir + ", libraries: " + this.libraries + ", include: " + this.include + ", exclude: "
		       + this.exclude + ", mainType: " + this.mainType + ", mainArgs: " + this.mainArgs + ")";
	}
}
