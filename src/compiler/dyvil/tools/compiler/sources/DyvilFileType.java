package dyvil.tools.compiler.sources;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.DyvilUnit;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.parsing.CodeFile;

import java.io.File;

public class DyvilFileType implements IFileType
{
	@FunctionalInterface
	interface HeaderSupplier
	{
		DyvilHeader newHeader(DyvilCompiler compiler, Package pack, CodeFile inputFile, File outputFile);
	}
	
	public static final String CLASS_EXTENSION  = ".class";
	public static final String OBJECT_EXTENSION = ".dyo";
	
	public static final IFileType DYVIL_UNIT   = new DyvilFileType("dyv", DyvilUnit::new);
	public static final IFileType DYVIL_HEADER = new DyvilFileType("dyh", DyvilHeader::new);

	public static void setupFileFinder(FileFinder fileFinder)
	{
		fileFinder.registerFileType("dyv", DYVIL_UNIT);
		fileFinder.registerFileType("dyvil", DYVIL_UNIT);
		fileFinder.registerFileType("dyh", DYVIL_HEADER);
		fileFinder.registerFileType("dyvilh", DYVIL_HEADER);
	}
	
	protected String         extension;
	protected HeaderSupplier headerSupplier;
	
	public DyvilFileType(String extension, HeaderSupplier headerSupplier)
	{
		this.extension = extension;
		this.headerSupplier = headerSupplier;
	}
	
	@Override
	public String getExtension()
	{
		return this.extension;
	}
	
	@Override
	public ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, CodeFile inputFile, File outputFile)
	{
		DyvilHeader header = this.headerSupplier.newHeader(compiler, pack, inputFile, outputFile);
		pack.addHeader(header);
		return header;
	}
}
