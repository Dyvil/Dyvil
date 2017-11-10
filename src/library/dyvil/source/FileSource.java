package dyvil.source;

import dyvil.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileSource extends TextSource
{
	private final File inputFile;

	public FileSource(File inputFile)
	{
		this.inputFile = inputFile;
	}

	public File file()
	{
		return this.inputFile;
	}

	public void load() throws IOException
	{
		if (this.text != null)
		{
			return;
		}

		this.text = FileUtils.read(this.inputFile);
		this.read(this.text);
	}

	@Override
	public String toString()
	{
		return this.inputFile.toString();
	}
}
