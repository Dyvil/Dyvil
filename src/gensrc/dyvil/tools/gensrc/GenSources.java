package dyvil.tools.gensrc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenSources
{
	public static final String TARGET_PREFIX = "target=";
	public static final String SOURCE_PREFIX = "source=";

	private List<Template>            templates       = new ArrayList<>();
	private Map<File, Specialization> specializations = new HashMap<>();

	private File sourceDir;
	private File targetDir;

	public static void main(String[] args)
	{
		final GenSources instance = new GenSources();
		instance.processArguments(args);
		instance.findFiles();
		instance.processTemplates();
	}

	private void processArguments(String[] args)
	{
		String sourceDir = null;
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
					System.out.println("Invalid -s argument: Source Directory expected");
				}
				else
				{
					sourceDir = args[i];
				}
				continue;
			case "-t":
			case "--target":
				if (++i == size)
				{
					System.out.println("Invalid -t argument: Target Directory expected");
				}
				continue;
			}
			if (arg.startsWith(SOURCE_PREFIX))
			{
				sourceDir = arg.substring(SOURCE_PREFIX.length());
			}
			else if (arg.startsWith(TARGET_PREFIX))
			{
				targetDir = arg.substring(TARGET_PREFIX.length());
			}
			else if (sourceDir == null)
			{
				sourceDir = arg;
			}
			else if (targetDir == null)
			{
				targetDir = arg;
			}
			else
			{
				System.out.println("Invalid Argument: " + arg);
			}
		}

		if (sourceDir == null)
		{
			System.out.println("Missing Source Directory");
			return;
		}
		if (targetDir == null)
		{
			System.out.println("Missing Target Directory");
			return;
		}

		this.sourceDir = new File(sourceDir);
		this.targetDir = new File(targetDir);
	}

	private void findFiles()
	{
		this.findFiles(this.sourceDir, this.targetDir);
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
				final String fileName = subFile.substring(0, dashIndex);
				final String specName = subFile.substring(dashIndex + 1, endIndex);

				specializations.add(new Specialization(sourceFile, fileName, specName));
			}
		}

		for (Specialization spec : specializations)
		{
			this.specializations.put(spec.getSourceFile(), spec);

			final Template template = templates.get(spec.getTemplateName());
			if (template == null)
			{
				System.out.println("Unassociated Specialization: " + spec.getSourceFile());
				continue;
			}

			spec.setTemplate(template);
			template.addSpecialization(spec);
		}

		this.templates.addAll(templates.values());
	}

	private void processTemplates()
	{
		for (Specialization spec : this.specializations.values())
		{
			spec.read(this.sourceDir, this.specializations);
		}
		for (Template template : this.templates)
		{
			template.specialize();
		}
	}
}
