package dyvil.tools.gensrc.ast;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.tools.gensrc.ast.scope.TemplateScope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.gensrc.parser.Parser;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.source.FileSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Template
{
	private final FileSource sourceFile;
	private final File       targetDirectory;
	private final String     fileName;

	private List<Specialization> specializations = new ArrayList<>();

	private DirectiveList directives;

	public Template(File sourceFile, File targetDir, String fileName)
	{
		this.sourceFile = new FileSource(sourceFile);
		this.targetDirectory = targetDir;
		this.fileName = fileName;
	}

	public FileSource getSourceFile()
	{
		return this.sourceFile;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public void addSpecialization(Specialization spec)
	{
		this.specializations.add(spec);
	}

	private boolean load(MarkerList markers)
	{
		try
		{
			this.sourceFile.load();
		}
		catch (IOException ignored)
		{
			return false;
		}

		this.directives = new Parser(this.sourceFile, markers).parse();
		return true;
	}

	public void specialize(GenSrc gensrc)
	{
		if (!this.targetDirectory.exists() && !this.targetDirectory.mkdirs())
		{
			gensrc.getOutput().println(I18n.get("template.directory.error", this.targetDirectory));
			return;
		}

		final MarkerList markers = new MarkerList(I18n.INSTANCE);
		if (!this.load(markers))
		{
			gensrc.getOutput().println(I18n.get("template.file.error", this.sourceFile.getInputFile()));
			return;
		}

		if (markers.getErrors() > 0)
		{
			this.printMarkers(gensrc, markers);
			return;
		}

		final int count = this.specializeAll(gensrc);

		gensrc.getOutput().println(I18n.get("template.specialized", count, this.sourceFile.getInputFile()));
	}

	private void printMarkers(GenSrc gensrc, MarkerList markers)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(I18n.get("template.problems", this.sourceFile.getInputFile())).append('\n').append('\n');

		final boolean colors = gensrc.useAnsiColors();
		for (Marker marker : markers)
		{
			marker.log(this.sourceFile, builder, colors);
		}
		gensrc.getOutput().println(builder);
	}

	private int specializeAll(GenSrc gensrc)
	{
		if (this.specializations.isEmpty())
		{
			this.specialize(gensrc, Specialization.createDefault(this.fileName));
			return 1;
		}

		int count = 0;
		for (Specialization spec : this.specializations)
		{
			if (spec.isEnabled())
			{
				this.specialize(gensrc, spec);
				count++;
			}
		}
		return count;
	}

	private void specialize(GenSrc gensrc, Specialization spec)
	{
		final String fileName = spec.getFileName();
		if (fileName == null)
		{
			return;
		}

		final File outputFile = new File(this.targetDirectory, fileName);
		final TemplateScope scope = new TemplateScope(this.sourceFile.getInputFile(), spec);

		try (final PrintStream writer = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile))))
		{
			this.directives.specialize(gensrc, scope, writer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace(gensrc.getErrorOutput());
		}
	}
}
