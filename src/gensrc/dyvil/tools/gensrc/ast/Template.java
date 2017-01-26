package dyvil.tools.gensrc.ast;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.tools.gensrc.ast.scope.TemplateScope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.gensrc.parser.Parser;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Template
{
	private final File   sourceFile;
	private final File   targetDirectory;
	private final String fileName;

	private List<Specialization> specializations = new ArrayList<>();

	private DirectiveList directives;

	public Template(File sourceFile, File targetDir, String fileName)
	{
		this.sourceFile = sourceFile;
		this.targetDirectory = targetDir;
		this.fileName = fileName;
	}

	public File getSourceFile()
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

	private DirectiveList getDirectives(GenSrc gensrc)
	{
		if (this.directives != null)
		{
			return this.directives;
		}

		try
		{
			final List<String> lines = Files.readAllLines(this.sourceFile.toPath());
			this.directives = new Parser(lines).parse();
		}
		catch (IOException e)
		{
			e.printStackTrace(gensrc.getErrorOutput());
		}

		return this.directives;
	}

	public void specialize(GenSrc gensrc)
	{
		if (!this.targetDirectory.exists() && !this.targetDirectory.mkdirs())
		{
			gensrc.getOutput().println("Could not create directory '" + this.targetDirectory + "'");
			return;
		}

		final int count = this.specializeAll(gensrc);

		gensrc.getOutput().println(I18n.get("template.specialized", count, this.getSourceFile()));
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
		final TemplateScope scope = new TemplateScope(this.sourceFile, spec);

		try (final PrintStream writer = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile))))
		{
			this.getDirectives(gensrc).specialize(gensrc, scope, writer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace(gensrc.getErrorOutput());
		}
	}
}
