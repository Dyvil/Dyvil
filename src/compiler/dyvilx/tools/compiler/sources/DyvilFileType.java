package dyvilx.tools.compiler.sources;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ClassUnit;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.ast.header.ISourceHeader;
import dyvilx.tools.compiler.ast.header.SourceHeader;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.lang.I18n;

import java.io.File;

public class DyvilFileType implements FileType
{
	@FunctionalInterface
	interface HeaderSupplier
	{
		ISourceHeader newHeader(DyvilCompiler compiler, Package pack, File inputFile, File outputFile);
	}

	public static final String DYVIL_EXTENSION  = ".dyv";
	public static final String HEADER_EXTENSION = ".dyh";

	public static final String CLASS_EXTENSION  = ".class";
	public static final String OBJECT_EXTENSION = ".dyo";

	public static final FileType DYVIL_UNIT   = new DyvilFileType("unit", ClassUnit::new);
	public static final FileType DYVIL_HEADER = new DyvilFileType("header", SourceHeader::new);

	public static void setupFileFinder(FileFinder fileFinder)
	{
		fileFinder.registerFileType(DYVIL_EXTENSION, DYVIL_UNIT);
		fileFinder.registerFileType(".dyvil", DYVIL_UNIT); // legacy
		fileFinder.registerFileType(HEADER_EXTENSION, DYVIL_HEADER);
		fileFinder.registerFileType(".dyvilh", DYVIL_HEADER); // legacy
	}

	protected final String         identifier;
	protected final HeaderSupplier headerSupplier;

	public DyvilFileType(String identifier, HeaderSupplier headerSupplier)
	{
		this.identifier = identifier;
		this.headerSupplier = headerSupplier;
	}

	@Override
	public String getLocalizedName()
	{
		return I18n.get("unit.filetype." + this.identifier);
	}

	@Override
	public ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, File inputFile, File outputFile)
	{
		final ISourceHeader header = this.headerSupplier.newHeader(compiler, pack, inputFile, outputFile);
		pack.addHeader(header);
		return header;
	}
}
