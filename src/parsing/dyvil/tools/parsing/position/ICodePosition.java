package dyvil.tools.parsing.position;

public interface ICodePosition
{
	ICodePosition ORIGIN = new CodePosition(1, 0, 1);

	static ICodePosition between(ICodePosition start, ICodePosition end)
	{
		return new CodePosition(start.endLine(), end.startLine(), start.endIndex(), end.startIndex());
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
