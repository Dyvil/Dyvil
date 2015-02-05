package dyvil.tools.compiler.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.library.Library;

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
	
	public boolean compileFile(String name)
	{
		for (String s : this.excludedFiles)
		{
			if (name.endsWith(s))
			{
				return false;
			}
		}
		
		for (String s : this.includedFiles)
		{
			if (name.endsWith(s))
			{
				return true;
			}
		}
		
		return true;
	}
	
	public String getJarName()
	{
		return String.format(this.jarNameFormat, this.jarName, this.jarVersion);
	}
	
	public String[] getMainArgs()
	{
		return this.mainArgs.toArray(new String[this.mainArgs.size()]);
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
