package dyvil.tools.parsing.source;

import java.util.Arrays;

public class TextSource implements Source
{
	protected static final int EXPECTED_LINE_LENGTH = 80;

	protected String text;
	protected int[]  lineStarts;
	protected int lineCount;

	public TextSource()
	{
	}

	public TextSource(String text)
	{
		this.read(text);
	}

	@Override
	public int lineCount()
	{
		return this.lineCount;
	}

	public void read(String text)
	{
		this.text = text;

		final int length = this.text.length();
		this.lineStarts = new int[length / EXPECTED_LINE_LENGTH + 1];

		this.lineCount = 1;

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
		if (index == this.lineCount)
		{
			return this.text.substring(this.lineStarts[index - 1], this.lineEnd(this.text.length()));
		}
		if (index < this.lineCount)
		{
			return this.text.substring(this.lineStarts[index - 1], this.lineEnd(this.lineStarts[index]));
		}
		return null;
	}

	private int lineEnd(int index)
	{
		char c = this.text.charAt(index - 1);
		switch (c)
		{
		case '\n':
			if (this.text.charAt(index - 2) == '\r')
			{
				return index - 2;
			}
			// Fallthrough
		case '\r':
			return index - 1;
		}
		return index;
	}
}
