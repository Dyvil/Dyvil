package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.sources.FileFinder;

public class CompilerConfig
{
	private String directory = ".";
	
	private String	jarName;
	private String	jarVendor;
	private String	jarVersion;
	private String	jarNameFormat	= "%1$s-%2$s.jar";
	
	private File				logFile;
	private File				sourceDir;
	private File				outputDir;
	public final List<Library>	libraries	= new ArrayList();
	
	public final List<String>	includedFiles	= new ArrayList();
	public final List<String>	excludedFiles	= new ArrayList();
	
	private String				mainType;
	public final List<String>	mainArgs	= new ArrayList();
	
	public CompilerConfig()
	{
		this.addLibrary(Library.dyvilLibrary);
		this.addLibrary(Library.javaLibrary);
		
		if (Library.dyvilBinLibrary != null)
		{
			this.addLibrary(Library.dyvilBinLibrary);
		}
	}
	
	public void setDirectory(String directory)
	{
		this.directory = directory;
	}
	
	public void setConfigFile(File configFile)
	{
		this.directory = configFile.getParent();
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
	
	public File getOutputDir()
	{
		return this.outputDir;
	}
	
	public void setOutputDir(String outputDir)
	{
		this.outputDir = new File(this.directory, outputDir);
	}
	
	public File getSourceDir()
	{
		return this.sourceDir;
	}
	
	public void setSourceDir(String sourceDir)
	{
		this.sourceDir = new File(this.directory, sourceDir);
	}
	
	public File getLogFile()
	{
		return this.logFile;
	}
	
	public void setLogFile(String logFile)
	{
		this.logFile = new File(this.directory, logFile);
	}
	
	public void addLibraryFile(File file)
	{
		this.libraries.add(Library.load(file));
	}
	
	public void addLibrary(Library library)
	{
		this.libraries.add(library);
	}
	
	public void includeFile(String fileName)
	{
		this.includedFiles.add(fileName);
	}
	
	public void excludeFile(String fileName)
	{
		this.excludedFiles.add(fileName);
	}
	
	public boolean isExcluded(String name)
	{
		for (String s : this.excludedFiles)
		{
			if (name.endsWith(s))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void findUnits(FileFinder fileFinder)
	{
		if (!this.includedFiles.isEmpty())
		{
			for (String s : this.includedFiles)
			{
				File source = new File(this.sourceDir, s);
				File output = new File(this.outputDir, s);
				Package pack = packageFromFile(s, source.isDirectory());
				
				fileFinder.process(source, output, pack);
			}
			return;
		}
		
		fileFinder.process(this.sourceDir, this.outputDir, Package.rootPackage);
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
	
	public String[] getMainArgs()
	{
		return this.mainArgs.toArray(String.class);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CompilerConfig [jarName=").append(this.getJarName());
		builder.append(", sourceDir=").append(this.sourceDir);
		builder.append(", outputDir=").append(this.outputDir);
		builder.append(", libraryFiles=").append(this.libraries);
		builder.append(", includedFiles=").append(this.includedFiles);
		builder.append(", excludedFiles=").append(this.excludedFiles);
		builder.append(", mainType=").append(this.mainType);
		builder.append(", mainArgs=").append(this.mainArgs).append("]");
		return builder.toString();
	}
}
