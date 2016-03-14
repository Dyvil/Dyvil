package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.File;

public interface ICompilationUnit extends IASTNode
{
	static boolean printMarkers(DyvilCompiler compiler, MarkerList markers, String fileType, Name name, File inputFile, String source)
	{
		final int size = markers.size();
		if (size <= 0)
		{
			return false;
		}

		final StringBuilder builder = new StringBuilder("Problems in ").append(fileType).append(' ').append(inputFile)
		                                                               .append(":\n\n");

		final int warnings = markers.getWarnings();
		final int errors = markers.getErrors();

		markers.sort();
		for (Marker marker : markers)
		{
			marker.log(source, builder);
		}

		builder.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings)
		       .append(warnings == 1 ? " Warning" : " Warnings");

		compiler.log(builder.toString());
		if (errors > 0)
		{
			compiler.failCompilation();
			compiler.warn(name + " was not compiled due to errors in the Compilation Unit\n");
			return true;
		}
		return false;
	}
	
	default boolean isHeader()
	{
		return false;
	}
	
	File getInputFile();
	
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
