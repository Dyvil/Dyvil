package dyvil.tools.compiler.ast.structure;

import java.io.File;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.parsing.CodeFile;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public interface ICompilationUnit extends IASTNode
{
	public static boolean printMarkers(MarkerList markers, String fileType, Name name, CodeFile source)
	{
		int size = markers.size();
		if (size > 0)
		{
			StringBuilder buf = new StringBuilder("Problems in ").append(fileType).append(' ').append(source).append(":\n\n");
			String code = source.getCode();
			
			int warnings = markers.getWarnings();
			int errors = markers.getErrors();
			markers.sort();
			for (Marker marker : markers)
			{
				marker.log(code, buf);
			}
			buf.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings).append(warnings == 1 ? " Warning" : " Warnings");
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
	
	public default boolean isHeader()
	{
		return false;
	}
	
	public CodeFile getInputFile();
	
	public File getOutputFile();
	
	public void tokenize();
	
	public void parseHeader();
	
	public void resolveHeader();
	
	public void parse();
	
	public void resolveTypes();
	
	public void resolve();
	
	public void checkTypes();
	
	public void check();
	
	public void foldConstants();
	
	public void cleanup();
	
	public void compile();
}
