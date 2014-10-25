package dyvil.tools.compiler.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.bytecode.ClassReader;

public class CompilerConfig
{
	public String		jarName;
	public String		jarGroup;
	public String		jarVersion;
	
	public File			sourceDir;
	public File			outputDir;
	public List<File>	libraryFiles	= new ArrayList();
	
	public List<String>	includedFiles	= new ArrayList();
	public List<String>	excludedFiles	= new ArrayList();
	
	public String		mainType;
	
	public CompilerConfig()
	{
		this.libraryFiles.add(ClassReader.rtJar);
	}
	
	public void addLibraryFile(File libraryFile)
	{
		this.libraryFiles.add(libraryFile);
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
			if (name.startsWith(s))
			{
				return false;
			}
		}
		
		for (String s : this.includedFiles)
		{
			if (name.startsWith(s))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CompilerConfig [jarName=").append(this.jarName);
		builder.append(", jarGroup=").append(this.jarGroup);
		builder.append(", jarVersion=").append(this.jarVersion);
		builder.append(", sourceDir=").append(this.sourceDir);
		builder.append(", outputDir=").append(this.outputDir);
		builder.append(", libraryFiles=").append(this.libraryFiles);
		builder.append(", includedFiles=").append(this.includedFiles);
		builder.append(", excludedFiles=").append(this.excludedFiles);
		builder.append(", mainType=").append(this.mainType).append("]");
		return builder.toString();
	}
}
