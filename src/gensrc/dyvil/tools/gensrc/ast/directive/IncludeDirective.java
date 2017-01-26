package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.file.Files;

public class IncludeDirective implements Directive
{
	private final String files;

	public IncludeDirective(String files)
	{
		this.files = files;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		final String[] fileNames = Util.getProcessedArguments(this.files, 0, this.files.length(), scope);
		final File sourceFile = scope.getSourceFile();

		for (String fileName : fileNames)
		{
			final File file = Specialization.resolveSpecFile(gensrc, fileName, sourceFile);

			if (!file.exists())
			{
				continue;
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
