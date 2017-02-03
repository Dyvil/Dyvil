package dyvil.tools.parsing;

import dyvil.io.FileUtils;
import dyvil.tools.parsing.source.Source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileSource implements Source
{
	private static final int EXPECTED_LINE_LENGTH = 80;

	private final File inputFile;

	private String text;
	private int[]  lineStarts;
	private int lineCount = 1;

	public FileSource(File inputFile)
	{
		this.inputFile = inputFile;
	}

	public File getInputFile()
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

		final int length = this.text.length();
		this.lineStarts = new int[length / EXPECTED_LINE_LENGTH];

		for (int i = 0; i < length; i++)
		{
			switch (this.text.charAt(i))
			{
			case '\r':
				if (i + 1 < length && this.text.charAt(i + 1) == '\n')
				{
					i++;
				}
				// Fallthrough
			case '\n':
				this.addLine(i + 1);
			}
		}
	}

	private void addLine(int sourceIndex)
	{
		int index = this.lineCount++;
		if (index >= this.lineStarts.length)
		{
			this.lineStarts = Arrays.copyOf(this.lineStarts, index << 1);
		}
		this.lineStarts[index] = sourceIndex;
	}

	@Override
	public String getText()
	{
		return this.text;
	}

	@Override
	public String getLine(int index)
	{
		if (index <= this.lineCount)
		{
			return this.text.substring(this.lineStarts[index], this.lineStarts[index + 1]);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return this.inputFile.toString();
	}
}
