package dyvil.tools.compiler.lexer.position;

import dyvil.tools.compiler.lexer.CodeFile;

public class CodePosition implements ICodePosition
{
	public CodeFile file;
	
	public String text;
	
	public int line;
	public int start;
	public int end;
	
	public CodePosition(CodeFile file, int line, int start, int end)
	{
		this(file, file.getCode().substring(start, end), line, start, end);
	}
	
	public CodePosition(CodeFile file, String text, int line, int start, int end)
	{
		this.file = file;
		this.text = text;
		this.line = line;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public CodeFile getFile()
	{
		return this.file;
	}
	
	@Override
	public String getText()
	{
		return this.text;
	}
	
	@Override
	public int getLineNumber()
	{
		return this.line;
	}
	
	@Override
	public int getStart()
	{
		return this.start;
	}
	
	@Override
	public int getEnd()
	{
		return this.end;
	}
}
