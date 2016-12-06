package dyvil.tools.compiler.sources;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.header.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;

import java.io.File;

public interface IFileType
{
	String getExtension();

	String getLocalizedName();
	
	ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, File inputFile, File outputFile);
}
