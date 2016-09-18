package dyvil.tools.gensrc;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Template
{
	private final File   sourceFile;
	private final File targetDirectory;
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

	public void specialize()
	{
		if (!this.targetDirectory.exists() && !this.targetDirectory.mkdirs())
		{
			System.out.println("Could not create directory '" + this.targetDirectory + "'");
			return;
		}

		// Remove disabled specs
		for (Iterator<Specialization> iterator = this.specializations.iterator(); iterator.hasNext(); )
		{
			if (!iterator.next().isEnabled())
			{
				iterator.remove();
			}
		}

		try
		{
			final List<String> lines = Files.readAllLines(this.sourceFile.toPath());

			for (Specialization spec : this.specializations)
			{
				this.specialize(lines, spec);
			}

			System.out.printf("Applied %d specializations for template '%s'\n", this.specializations.size(), this.getSourceFile());
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
			System.out.printf("Invalid Specialization '%s' (%s): Missing 'fileName' property'\n", spec.getName(),
			                  spec.getSourceFile());
			return;
		}

		final File outputFile = new File(this.targetDirectory, fileName);

		try (final PrintStream writer = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile))))
		{
			spec.processLines(lines, writer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
