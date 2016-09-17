package dyvil.tools.gensrc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GenSources
{
	public static final String TARGET_PREFIX = "target=";
	public static final String SOURCE_PREFIX = "source=";

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Not enough arguments. Source and target directory expected");
			return;
		}

		String sourceDir = null;
		String targetDir = null;
		for (String s : args)
		{
			if (s.startsWith(SOURCE_PREFIX))
			{
				sourceDir = s.substring(SOURCE_PREFIX.length());
			}
			else if (s.startsWith(TARGET_PREFIX))
			{
				targetDir = s.substring(TARGET_PREFIX.length());
			}
			else if (sourceDir == null)
			{
				sourceDir = s;
			}
			else if (targetDir == null)
			{
				targetDir = s;
			}
			else
			{
				System.out.println("Invalid Argument: " + s);
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

		processSources(new File(sourceDir), new File(targetDir));
	}

	private static void processSources(File sourceDir, File targetDir)
	{
		final String[] subFiles = sourceDir.list();
		if (subFiles == null)
		{
			return;
		}

		Map<String, Template> templates = new TreeMap<>();
		List<Specialization> specializations = new ArrayList<>();

		for (String subFile : subFiles)
		{
			final File sourceFile = new File(sourceDir, subFile);
			if (sourceFile.isDirectory())
			{
				processSources(sourceFile, new File(targetDir, subFile));
				continue;
			}

			final int endIndex = subFile.length() - 4;
			if (subFile.endsWith(".dgt"))
			{
				final String fileName = subFile.substring(0, endIndex);
				templates.put(fileName, new Template(sourceFile, fileName));
			}
			else if (subFile.endsWith(".dgs"))
			{
				final int dashIndex = subFile.lastIndexOf('-', endIndex);
				final String fileName = subFile.substring(0, dashIndex);
				final String specName = subFile.substring(dashIndex + 1, endIndex);

				specializations.add(new Specialization(sourceFile, fileName, specName));
			}
		}

		int specCount = 0;
		for (Specialization spec : specializations)
		{
			final Template template = templates.get(spec.getTemplateName());
			if (template == null)
			{
				System.out.println("Unassociated Specialization: " + spec.getSourceFile());
				continue;
			}

			spec.read();
			if (!spec.isEnabled())
			{
				continue;
			}

			specCount++;
			template.addSpecialization(spec);
		}

		for (Template template : templates.values())
		{
			template.specialize(targetDir);
		}

		final int templateCount = templates.size();

		if (templateCount != 0)
		{
			System.out
				.printf("Applied %d specializations for %d templates from '%s' to '%s'\n", specCount, templateCount,
				        sourceDir, targetDir);
		}
	}
}
