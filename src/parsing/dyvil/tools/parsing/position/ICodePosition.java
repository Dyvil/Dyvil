package dyvil.tools.parsing.position;

public interface ICodePosition
{
	ICodePosition ORIGIN = new CodePosition(1, 0, 1);

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

	default boolean before(ICodePosition position)
	{
		return this.endIndex() < position.startIndex();
	}

	default boolean after(ICodePosition position)
	{
		return this.startIndex() > position.endIndex();
	}
}
