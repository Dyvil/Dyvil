package dyvil.tools.compiler.lexer.position;

import dyvil.tools.compiler.lexer.CodeFile;

public class CodePosition implements ICodePosition
{
	public CodeFile	file;
	
	public int		type;
	public String	text;
	
	public int		line;
	public int		start;
	public int		end;
	
	public CodePosition(CodeFile file, int type, int line, int start, int end)
	{
		this(file, type, file.getCode().substring(start, end), line, start, end);
	}
	
	public CodePosition(CodeFile file, int type, String text, int line, int start, int end)
	{
		this.file = file;
		this.type = type;
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
	public int getType()
	{
		return this.type;
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
