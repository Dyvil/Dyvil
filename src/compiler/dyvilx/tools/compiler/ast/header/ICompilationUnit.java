package dyvilx.tools.compiler.ast.header;

import dyvil.io.Console;
import dyvil.io.StringBuilderWriter;
import dyvil.lang.Name;
import dyvil.source.FileSource;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.sources.FileType;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.MarkerPrinter;
import dyvilx.tools.parsing.marker.MarkerStyle;

public interface ICompilationUnit extends ASTNode
{
	FileSource getFileSource();

	MarkerList getMarkers();

	void tokenize();

	void parse();

	void resolveHeaders();

	void resolveTypes();

	void resolve();

	void checkTypes();

	void check();

	void foldConstants();

	void cleanup();

	void compile();

	static boolean printMarkers(DyvilCompiler compiler, MarkerList markers, FileType fileType, Name name,
		                           FileSource source)
	{
		final int size = markers.size();
		if (size <= 0)
		{
			return false;
		}

		final StringBuilder builder = new StringBuilder(I18n.get("unit.problems", fileType.getLocalizedName(), name,
		                                                         source.file())).append("\n\n");

		final int warnings = markers.getWarnings();
		final int errors = markers.getErrors();
		final MarkerStyle style = compiler.config.getMarkerStyle();
		final boolean colors = compiler.config.useAnsiColors();

		new MarkerPrinter(source, style, colors).print(markers, new StringBuilderWriter(builder));

		if (errors > 0)
		{
			if (colors)
			{
				builder.append(Console.ANSI_RED);
			}

			builder.append(errors == 1 ? I18n.get("unit.errors.1") : I18n.get("unit.errors.n", errors));

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
			builder.append(warnings == 1 ? I18n.get("unit.warnings.1") : I18n.get("unit.warnings.n", warnings));

			if (colors)
			{
				builder.append(Console.ANSI_RESET);
			}
		}

		builder.append('\n');

		compiler.log(builder.toString());
		if (errors > 0)
		{
			compiler.fail();
			compiler.warn(I18n.get("unit.problems.not_compiled", name));
			compiler.warn("");
			return true;
		}
		return false;
	}
}
