package dyvil.tools.compiler.lexer.position;

public interface ICodePosition
{
	public int startIndex();
	
	public int endIndex();
	
	public int startLine();
	
	public int endLine();
	
	public ICodePosition raw();
	
	public ICodePosition to(ICodePosition end);
}
