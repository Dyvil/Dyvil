package dyvil.tools.parsing.position;

public interface ICodePosition
{
	ICodePosition ORIGIN = new CodePosition(1, 0, 1);

	static ICodePosition before(ICodePosition next)
	{
		final int startLine = next.startLine();
		int startIndex = next.startIndex();
		if (startIndex == 0)
		{
			startIndex++;
		}
		return new CodePosition(startLine, startLine, startIndex - 1, startIndex);
	}

	static ICodePosition after(ICodePosition prev)
	{
		final int endLine = prev.endLine();
		final int endIndex = prev.endIndex();
		return new CodePosition(endLine, endLine, endIndex, endIndex + 1);
	}

	static ICodePosition between(ICodePosition start, ICodePosition end)
	{
		int startIndex = start.endIndex();
		int endIndex = end.startIndex();
		if (startIndex == endIndex)
		{
			startIndex--;
			endIndex++;
		}
		return new CodePosition(start.endLine(), end.startLine(), startIndex, endIndex);
	}

	int startIndex();

	int endIndex();

	int startLine();

	int endLine();

	ICodePosition raw();

	ICodePosition to(ICodePosition end);

	default boolean isBefore(ICodePosition position)
	{
		return this.endIndex() < position.startIndex();
	}

	default boolean isAfter(ICodePosition position)
	{
		return this.startIndex() > position.endIndex();
	}
}
