package dyvil.tools.compiler.sources;

import java.io.File;

import dyvil.lang.Map;

import dyvil.collection.mutable.HashMap;
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
																		DyvilUnit unit = new DyvilUnit(pack, inputFile, outputFile);
																		pack.addHeader(unit);
																		return unit;
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
																		DyvilHeader header = new DyvilHeader(pack, inputFile, outputFile);
																		pack.addHeader(header);
																		return header;
																	}
																};
	
	static
	{
		fileTypes.put("dyv", DYVIL_UNIT);
		fileTypes.put("dyvil", DYVIL_UNIT);
		fileTypes.put("dyh", DYVIL_HEADER);
		fileTypes.put("dyvilh", DYVIL_HEADER);
	}
}
