package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.sources.FileFinder;

public class CompilerConfig
{
	public String			jarName;
	public String			jarVendor;
	public String			jarVersion;
	public String			jarNameFormat	= "%1$s-%2$s.jar";
	
	public File				sourceDir;
	public File				outputDir;
	public List<Library>	libraries		= new ArrayList();
	
	public List<String>		includedFiles	= new ArrayList();
	public List<String>		excludedFiles	= new ArrayList();
	
	public String			mainType;
	public List<String>		mainArgs		= new ArrayList();
	
	public CompilerConfig()
	{
		this.addLibrary(Library.dyvilLibrary);
		this.addLibrary(Library.javaLibrary);
		
		if (Library.dyvilBinLibrary != null)
		{
			this.addLibrary(Library.dyvilBinLibrary);
		}
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
				File source = new File(sourceDir, s);
				File output = new File(outputDir, s);
				Package pack = packageFromFile(s, source.isDirectory());
				
				fileFinder.findUnits(source, output, pack);
			}
			return;
		}
		
		fileFinder.findUnits(sourceDir, outputDir, Package.rootPackage);
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
