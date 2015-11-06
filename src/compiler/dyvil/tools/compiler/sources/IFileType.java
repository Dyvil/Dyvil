package dyvil.tools.compiler.sources;

import java.io.File;

import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.parsing.CodeFile;

public interface IFileType
{
	public String getExtension();
	
	public ICompilationUnit createUnit(Package pack, CodeFile inputFile, File outputFile);
}
