package dyvil.tools.gensrc;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Template
{
	private final File   sourceFile;
	private final String fileName;

	private List<Specialization> specializations = new ArrayList<>();

	public Template(File sourceFile, String fileName)
	{
		this.sourceFile = sourceFile;
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

	public void specialize(File targetDirectory)
	{
		if (!targetDirectory.exists() && !targetDirectory.mkdirs())
		{
			System.out.println("Could not create directory '" + targetDirectory + "'");
			return;
		}

		try
		{
			final List<String> lines = Files.readAllLines(this.sourceFile.toPath());

			for (Specialization spec : this.specializations)
			{
				this.specialize(targetDirectory, lines, spec);
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void specialize(File targetDirectory, List<String> lines, Specialization spec)
	{
		final String fileName = spec.getFileName();
		if (fileName == null)
		{
			System.out.printf("Invalid Specialization '%s' (%s): Missing 'fileName' property'\n", spec.getName(),
			                  spec.getSourceFile());
			return;
		}

		final File outputFile = new File(targetDirectory, fileName);

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
