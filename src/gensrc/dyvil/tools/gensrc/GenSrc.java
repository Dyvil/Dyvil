package dyvil.tools.gensrc;

import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Template;
import dyvil.tools.gensrc.lang.I18n;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenSrc
{
	private List<Template>            templates = new ArrayList<>();
	private Map<File, Specialization> specs     = new HashMap<>();

	private File sourceRoot;
	private File targetRoot;

	private final PrintStream output;
	private final PrintStream errorOutput;

	private boolean ansiColors;

	public GenSrc(PrintStream output, PrintStream errorOutput)
	{
		this.output = output;
		this.errorOutput = errorOutput;
	}

	public File getSourceRoot()
	{
		return this.sourceRoot;
	}

	public void setSourceRoot(File sourceRoot)
	{
		this.sourceRoot = sourceRoot;
	}

	public File getTargetRoot()
	{
		return this.targetRoot;
	}

	public void setTargetRoot(File targetRoot)
	{
		this.targetRoot = targetRoot;
	}

	public boolean useAnsiColors()
	{
		return this.ansiColors;
	}

	public void setAnsiColors(boolean ansiColors)
	{
		this.ansiColors = ansiColors;
	}

	public PrintStream getOutput()
	{
		return this.output;
	}

	public PrintStream getErrorOutput()
	{
		return this.errorOutput;
	}

	public Specialization getSpecialization(File file)
	{
		return this.specs.get(file);
	}

	public void findFiles()
	{
		this.findFiles(this.sourceRoot, this.targetRoot);
	}

	public void findFiles(File sourceDir, File targetDir)
	{
		final String[] subFiles = sourceDir.list();
		if (subFiles == null)
		{
			return;
		}

		Map<String, Template> templates = new HashMap<>();
		List<Specialization> specializations = new ArrayList<>();

		for (String subFile : subFiles)
		{
			final File sourceFile = new File(sourceDir, subFile);
			if (sourceFile.isDirectory())
			{
				this.findFiles(sourceFile, new File(targetDir, subFile));
				continue;
			}

			final int endIndex = subFile.length() - 4;
			if (subFile.endsWith(".dgt"))
			{
				final String fileName = subFile.substring(0, endIndex);
				templates.put(fileName, new Template(sourceFile, targetDir, fileName));
			}
			else if (subFile.endsWith(".dgs"))
			{
				final int dashIndex = subFile.lastIndexOf('-', endIndex);
				final Specialization spec;

				if (dashIndex < 0)
				{
					// Spec name "default"
					spec = new Specialization(sourceFile, subFile.substring(0, endIndex));
				}
				else
				{
					// Custom spec name
					final String fileName = subFile.substring(0, dashIndex);
					final String specName = subFile.substring(dashIndex + 1, endIndex);
					spec = new Specialization(sourceFile, fileName, specName);
				}


				specializations.add(spec);
			}
		}

		for (Specialization spec : specializations)
		{
			this.specs.put(spec.getSourceFile(), spec);

			final Template template = templates.get(spec.getTemplateName());
			if (template == null)
			{
				continue;
			}

			spec.setTemplate(template);
			template.addSpecialization(spec);
		}

		this.templates.addAll(templates.values());
	}

	public void processTemplates()
	{
		for (Specialization spec : this.specs.values())
		{
			this.loadSpecialization(spec);
		}

		for (Template template : this.templates)
		{
			template.specialize(this);
		}
	}

	private void loadSpecialization(Specialization spec)
	{
		final List<String> markers = new ArrayList<>();
		spec.load(this, markers);

		if (markers.isEmpty())
		{
			return;
		}

		final StringBuilder builder = new StringBuilder(I18n.get("spec.problems", spec.getName(), spec.getSourceFile()))
			                              .append("\n\n");
		if (this.ansiColors)
		{
			builder.append("\u001B[31m"); // ANSI_RED
		}

		for (String marker : markers)
		{
			builder.append(marker).append('\n');
		}

		if (this.ansiColors)
		{
			builder.append("\u001B[0m"); // ANSI_RESET
		}

		this.output.println(builder.append('\n').toString());
	}
}
