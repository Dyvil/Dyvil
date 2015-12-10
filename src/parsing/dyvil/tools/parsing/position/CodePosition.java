package dyvil.tools.parsing.position;

public class CodePosition implements ICodePosition
{
	public int startLine;
	public int endLine;
	public int start;
	public int end;
	
	public CodePosition(int line, int start, int end)
	{
		this.startLine = line;
		this.endLine = line;
		this.start = start;
		this.end = end;
	}
	
	public CodePosition(int startLine, int endLine, int start, int end)
	{
		this.startLine = startLine;
		this.endLine = endLine;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		return this.end;
	}
	
	@Override
	public int startLine()
	{
		return this.startLine;
	}
	
	@Override
	public int endLine()
	{
		return this.endLine;
	}
	
	@Override
	public ICodePosition raw()
	{
		return this;
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.startLine, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "CodePosition(startIndex: " + this.start + ", endIndex: " + this.end + ", startLine: " + this.startLine
				+ ", endLine: " + this.endLine + ")";
	}
}
