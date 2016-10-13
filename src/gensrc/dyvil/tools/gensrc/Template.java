package dyvil.tools.gensrc;

import dyvil.tools.gensrc.lang.I18n;

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

	public void load(GenSources gensrc)
	{
		for (Specialization spec : this.specializations)
		{
			gensrc.loadSpecialization(spec);
		}
	}

	public void specialize()
	{
		if (!this.targetDirectory.exists() && !this.targetDirectory.mkdirs())
		{
			System.out.println("Could not create directory '" + this.targetDirectory + "'");
			return;
		}

		try
		{
			final List<String> lines = Files.readAllLines(this.sourceFile.toPath());
			int count = 0;

			for (Specialization spec : this.specializations)
			{
				if (spec.isEnabled())
				{
					this.specialize(lines, spec);
					count++;
				}
			}

			System.out.println(I18n.get("template.specialized", count, this.getSourceFile()));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void specialize(List<String> lines, Specialization spec)
	{
		final String fileName = spec.getFileName();
		if (fileName == null)
		{
			return;
		}

		final File outputFile = new File(this.targetDirectory, fileName);

		try (final PrintStream writer = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile))))
		{
			Specializer.processLines(lines, writer, spec);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
