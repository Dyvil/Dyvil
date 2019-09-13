package dyvilx.tools.compiler.sources;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.ast.structure.Package;

import java.io.File;

public interface FileType
{
	String getLocalizedName();

	ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, File inputFile, File outputFile);
}
