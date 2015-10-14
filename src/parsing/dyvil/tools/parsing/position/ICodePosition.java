package dyvil.tools.parsing.position;

public interface ICodePosition
{
	public static ICodePosition ORIGIN = new CodePosition(1, 0, 1);
	
	public int startIndex();
	
	public int endIndex();
	
	public int startLine();
	
	public int endLine();
	
	public ICodePosition raw();
	
	public ICodePosition to(ICodePosition end);
}
