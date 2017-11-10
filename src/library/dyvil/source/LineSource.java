package dyvil.source;

public class LineSource implements Source
{
	private final String line;

	public LineSource(String line)
	{
		this.line = line;
	}

	@Override
	public int lineCount()
	{
		return 1;
	}

	@Override
	public String text()
	{
		return this.line;
	}

	@Override
	public String line(int index)
	{
		return index == 1 ? this.line : null;
	}
}
