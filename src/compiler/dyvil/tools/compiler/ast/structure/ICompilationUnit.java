package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.parsing.CodeFile;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.File;

public interface ICompilationUnit extends IASTNode
{
	static boolean printMarkers(MarkerList markers, String fileType, Name name, CodeFile source)
	{
		int size = markers.size();
		if (size > 0)
		{
			StringBuilder buf = new StringBuilder("Problems in ").append(fileType).append(' ').append(source)
			                                                     .append(":\n\n");
			String code = source.getCode();
			
			int warnings = markers.getWarnings();
			int errors = markers.getErrors();
			markers.sort();
			for (Marker marker : markers)
			{
				marker.log(code, buf);
			}
			buf.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings)
			   .append(warnings == 1 ? " Warning" : " Warnings");
			DyvilCompiler.log(buf.toString());
			if (errors > 0)
			{
				DyvilCompiler.compilationFailed = true;
				DyvilCompiler.warn(name + " was not compiled due to errors in the Compilation Unit\n");
				return true;
			}
		}
		return false;
	}
	
	default boolean isHeader()
	{
		return false;
	}
	
	CodeFile getInputFile();
	
	File getOutputFile();
	
	void tokenize();
	
	void parseHeader();
	
	void resolveHeader();
	
	void parse();
	
	void resolveTypes();
	
	void resolve();
	
	void checkTypes();
	
	void check();
	
	void foldConstants();
	
	void cleanup();
	
	void compile();
}
