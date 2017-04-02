package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

public class IncludeDirective implements Directive
{
	private final ICodePosition position;
	private final String        files;

	public IncludeDirective(ICodePosition position, String files)
	{
		this.position = position;
		this.files = files;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		final String[] fileNames = Util.getProcessedArguments(this.files, 0, this.files.length(), scope);
		final File sourceFile = scope.getSourceFile();

		for (String fileName : fileNames)
		{
			this.resolveAndInclude(gensrc, markers, output, sourceFile, fileName);
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

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#include ").append(this.files).append('\n');
	}
}
