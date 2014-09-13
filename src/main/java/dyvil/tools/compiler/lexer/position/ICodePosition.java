package dyvil.tools.compiler.lexer.position;

import dyvil.tools.compiler.lexer.CodeFile;

public interface ICodePosition
{
	public CodeFile getFile();
	
	public String getText();
	
	public int getLineNumber();
	
	public int getStart();
	
	public int getEnd();
	
	public default int getPrevNewline()
	{
		int i = this.getFile().getCode().lastIndexOf('\n', this.getStart());
		if (i < 0)
			return 0;
		return i + 1;
	}
	
	public default int getNextNewline()
	{
		int i = this.getFile().getCode().indexOf('\n', this.getEnd());
		if (i < 0)
			return this.getFile().getLength() - 1;
		return i;
	}
	
	public default String getCurrentLine()
	{
		int prevNL = this.getPrevNewline();
		int nextNL = this.getNextNewline();
		return this.getFile().getCode().substring(prevNL, nextNL);
	}
}
