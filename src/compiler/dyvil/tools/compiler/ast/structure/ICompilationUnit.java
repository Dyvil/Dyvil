package dyvil.tools.compiler.ast.structure;

import dyvil.io.Console;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.File;

public interface ICompilationUnit extends IASTNode
{
	default boolean isHeader()
	{
		return false;
	}

	File getInputFile();

	File getOutputFile();

	void tokenize();

	void parse();

	void resolveTypes();

	void resolve();

	void checkTypes();

	void check();

	void foldConstants();

	void cleanup();

	void compile();

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
		final boolean colors = compiler.config.useAnsiColors();

		markers.sort();
		for (Marker marker : markers)
		{
			marker.log(source, builder, colors);
		}

		if (errors > 0)
		{
			if (colors)
			{
				builder.append(Console.ANSI_RED);
			}

			builder.append(errors).append(errors == 1 ? " Error" : " Errors");

			if (colors)
			{
				builder.append(Console.ANSI_RESET);
			}
		}
		if (warnings > 0)
		{
			if (errors > 0)
			{
				builder.append(", ");
			}

			if (colors)
			{
				builder.append(Console.ANSI_YELLOW);
			}
			builder.append(warnings).append(warnings == 1 ? " Warning" : " Warnings");

			if (colors)
			{
				builder.append(Console.ANSI_RESET);
			}
		}

		compiler.log(builder.toString());
		if (errors > 0)
		{
			compiler.failCompilation();
			compiler.warn(name + " was not compiled due to errors in the Compilation Unit\n");
			return true;
		}
		return false;
	}
}
