package dyvil.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

public class IncludeDirective extends BasicDirective
{
	public static final Name INCLUDE = Name.fromQualified("include");

	public IncludeDirective(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return INCLUDE;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		final File sourceFile = scope.getSourceFile();

		for (Expression expr : this.arguments)
		{
			final String reference = expr.evaluateString(scope);
			this.resolveAndInclude(gensrc, markers, output, sourceFile, reference);
		}
	}

	protected void resolveAndInclude(GenSrc gensrc, MarkerList markers, PrintStream output, File sourceFile,
		                                String fileName)
	{
		File file = null;
		for (File resolved : Specialization.resolveSpecFiles(fileName, sourceFile, gensrc))
		{
			if (file != null && !resolved.isDirectory() && !file.isDirectory())
			{
				file = null; // ambigous -> cause a file not found error
				break;
			}

			file = resolved;
		}

		if (file == null)
		{
			markers.add(new SemanticError(this.position, I18n.get("include.file.not_found", fileName)));
			return;
		}
		if (file.isDirectory())
		{
			markers.add(new SemanticError(this.position, I18n.get("include.file.directory", file)));
			return;
		}

		try
		{
			Files.copy(file.toPath(), output);
		}
		catch (IOException ex)
		{
			ex.printStackTrace(gensrc.getErrorOutput());
		}
	}
}
