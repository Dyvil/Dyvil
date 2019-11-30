package dyvilx.tools.compiler.ast.header;

import dyvil.io.Console;
import dyvil.lang.Name;
import dyvil.source.FileSource;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.sources.FileType;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.MarkerPrinter;
import dyvilx.tools.parsing.marker.MarkerStyle;

import java.io.IOException;
import java.io.OutputStreamWriter;

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

		final int errors = markers.getErrors();
		final MarkerStyle style = compiler.config.getMarkerStyle();
		final boolean colors = compiler.config.useAnsiColors();
		final MarkerPrinter printer = new MarkerPrinter(source, style, colors);

		if (compiler.config.isDebug())
		{
			compiler.log(I18n.get("unit.problems", fileType.getLocalizedName(), name, source.file()));
			compiler.log("");
		}

		// TODO v0.48.0: use style == MarkerStyle.JAVAC
		final OutputStreamWriter writer = new OutputStreamWriter(
			"JAVAC".equals(style.name()) ? compiler.getErrorOutput() : compiler.getOutput());
		printer.print(markers, writer);
		try
		{
			writer.flush();
		}
		catch (IOException ex)
		{
			ex.printStackTrace(compiler.getErrorOutput());
		}

		if (compiler.config.isDebug())
		{
			final StringBuilder summary = new StringBuilder();

			final int warnings = markers.getWarnings();
			if (warnings > 0)
			{
				final String warningsStr =
					warnings == 1 ? I18n.get("unit.warnings.1") : I18n.get("unit.warnings.n", warnings);

				if (colors)
				{
					Console.appendStyled(summary, warningsStr, Console.ANSI_YELLOW);
				}
				else
				{
					summary.append(warningsStr);
				}
			}

			if (errors > 0)
			{
				if (warnings > 0)
				{
					summary.append(", ");
				}

				if (colors)
				{
					summary.append(Console.ANSI_RED);
				}

				summary.append(errors == 1 ? I18n.get("unit.errors.1") : I18n.get("unit.errors.n", errors));
				summary.append(": ");
				summary.append(I18n.get("unit.problems.not_compiled", name, fileType.getLocalizedName()));

				if (colors)
				{
					summary.append(Console.ANSI_RESET);
				}
			}

			compiler.log(summary.toString());
		}

		if (errors > 0)
		{
			compiler.fail();
			return true;
		}
		return false;
	}
}
