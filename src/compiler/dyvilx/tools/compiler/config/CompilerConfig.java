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
	public final List<File> sourceDirs = new ArrayList<>();
	private File outputDir;
	public final List<Library> libraries = new ArrayList<>();

	public final List<Pattern> include = new ArrayList<>();
	public final List<Pattern> exclude = new ArrayList<>();

	private String mainType;
	public final List<String> mainArgs = new ArrayList<>();

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

	public File getOutputDir()
	{
		return this.outputDir;
	}

	public void setOutputDir(String outputDir)
	{
		this.outputDir = new File(outputDir);
	}

	public void addSourceDir(String sourceDir)
	{
		this.sourceDirs.add(new File(sourceDir));
	}

	public File getLogFile()
	{
		return this.logFile;
	}

	public void setLogFile(String logFile)
	{
		this.logFile = new File(logFile);
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

	public void addLibrary(Library library)
	{
		this.libraries.add(library);
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

	private static Package packageFromFile(String file, boolean isDirectory)
	{
		int index = 0;
		Package pack = Package.rootPackage;
		do
		{
			int nextIndex = file.indexOf('/', index + 1);
			if (nextIndex < 0)
			{
				return isDirectory ? pack.resolvePackage(file.substring(index)) : pack;
			}

			pack = pack.createSubPackage(file.substring(index, nextIndex));
			index = nextIndex + 1;
		}
		while (index < file.length());

		return pack;
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
