package dyvil.tools.compiler.sources;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.parsing.CodeFile;

import java.io.File;

public interface IFileType
{
	String getExtension();
	
	ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, CodeFile inputFile, File outputFile);
}
