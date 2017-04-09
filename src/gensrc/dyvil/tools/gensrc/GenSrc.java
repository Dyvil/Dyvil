package dyvil.tools.gensrc;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.Template;
import dyvil.tools.gensrc.lang.I18n;
import dyvilx.tools.BasicTool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GenSrc extends BasicTool
{
	public static final String TARGET_PREFIX = "target=";
	public static final String SOURCE_PREFIX = "source=";

	private List<Template>              templates = new ArrayList<>();
	private Map<String, Specialization> specs     = new HashMap<>();

	private List<File> sourceRoots = new ArrayList<>();
	private File targetRoot;

	private boolean ansiColors;

	public List<File> getSourceRoots()
	{
		return this.sourceRoots;
	}

	public void addSourceRoot(File sourceRoot)
	{
		this.sourceRoots.add(sourceRoot);
	}

	public File getTargetRoot()
	{
		return this.targetRoot;
	}

	public void setTargetRoot(File targetRoot)
	{
		this.targetRoot = targetRoot;
	}

	@Override
	public boolean useAnsiColors()
	{
		return this.ansiColors;
	}

	public void setAnsiColors(boolean ansiColors)
	{
		this.ansiColors = ansiColors;
	}

	@Override
	public int run(InputStream in, OutputStream out, OutputStream err, String... arguments)
	{
		this.initOutput(out, err);

		if (!this.processArguments(arguments))
		{
			return 1;
		}

		this.findFiles();
		this.processTemplates();

		return this.getExitCode();
	}

	private boolean processArguments(String[] args)
	{
		String targetDir = null;
		for (int i = 0, size = args.length; i < size; i++)
		{
			final String arg = args[i];

			switch (arg)
			{
			case "-s":
			case "--source":
				if (++i == size)
				{
					this.error("Invalid -s argument: Source Directory expected");
				}
				else
				{
					this.addSourceRoot(new File(args[i]));
				}
				continue;
			case "-t":
			case "--target":
				if (++i == size)
				{
					this.error("Invalid -t argument: Target Directory expected");
				}
				continue;
			case "--ansi":
				this.setAnsiColors(true);
				continue;
			}
			if (arg.startsWith(SOURCE_PREFIX))
			{
				this.addSourceRoot(new File(arg.substring(SOURCE_PREFIX.length())));
			}
			else if (arg.startsWith(TARGET_PREFIX))
			{
				targetDir = arg.substring(TARGET_PREFIX.length());
			}
			else
			{
				this.error("Invalid Argument: " + arg);
			}
		}

		if (this.sourceRoots.isEmpty())
		{
			this.error("Missing Source Directory");
			return false;
		}
		if (targetDir == null)
		{
			this.error("Missing Target Directory");
			return false;
		}

		this.setTargetRoot(new File(targetDir));
		return true;
	}

	public Specialization getSpecialization(File file)
	{
		try
		{
			return this.specs.get(file.getCanonicalPath());
		}
		catch (IOException ignored)
		{
			return null;
		}
	}

	private void findFiles()
	{
		for (File root : this.sourceRoots)
		{
			this.findFiles(root, this.targetRoot);
		}
	}

	private void findFiles(File sourceDir, File targetDir)
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
			try
			{
				this.specs.put(spec.getSourceFile().getCanonicalPath(), spec);
			}
			catch (IOException ignored)
			{
			}

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

	private void processTemplates()
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

		this.log(builder.append('\n').toString());
	}
}
