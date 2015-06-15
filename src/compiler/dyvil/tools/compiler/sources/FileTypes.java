package dyvil.tools.compiler.sources;

import java.io.File;

import dyvil.collection.mutable.HashMap;
import dyvil.lang.Map;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.DyvilUnit;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.CodeFile;

public class FileTypes
{
	public static final Map<String, IFileType>	fileTypes		= new HashMap();
	
	public static final IFileType				DYVIL_UNIT		= new IFileType()
																{
																	@Override
																	public String getExtension()
																	{
																		return "dyv";
																	}
																	
																	@Override
																	public ICompilationUnit createUnit(Package pack, CodeFile inputFile, File outputFile)
																	{
																		return new DyvilUnit(pack, inputFile, outputFile);
																	}
																};
	
	public static final IFileType				DYVIL_HEADER	= new IFileType()
																{
																	@Override
																	public String getExtension()
																	{
																		return "dyh";
																	}
																	
																	@Override
																	public ICompilationUnit createUnit(Package pack, CodeFile inputFile, File outputFile)
																	{
																		return new DyvilHeader(pack, inputFile, outputFile);
																	}
																};
	
	static
	{
		fileTypes.update("dyv", DYVIL_UNIT);
		fileTypes.update("dyvil", DYVIL_UNIT);
		fileTypes.update("dyh", DYVIL_HEADER);
		fileTypes.update("dyvilh", DYVIL_HEADER);
	}
}
