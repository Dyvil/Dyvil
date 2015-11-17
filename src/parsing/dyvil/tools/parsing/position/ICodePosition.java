package dyvil.tools.parsing.position;

public interface ICodePosition
{
	ICodePosition ORIGIN = new CodePosition(1, 0, 1);
	
	int startIndex();
	
	int endIndex();
	
	int startLine();
	
	int endLine();
	
	ICodePosition raw();
	
	ICodePosition to(ICodePosition end);
}
