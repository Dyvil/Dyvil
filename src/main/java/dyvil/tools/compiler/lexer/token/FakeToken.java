package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.SyntaxError;

public class FakeToken implements IToken
{
	private CodeFile	file;
	
	private IToken		prev;
	private IToken		next;
	
	public FakeToken(CodeFile file)
	{
		this.file = file;
	}
	
	@Override
	public String value() throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public Object object() throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public int type() throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public boolean equals(String value) throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public boolean isType(int type) throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public int index() throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public CodeFile getFile()
	{
		return this.file;
	}
	
	@Override
	public String getText()
	{
		return "";
	}
	
	@Override
	public int getLineNumber()
	{
		return -1;
	}
	
	@Override
	public int getStart()
	{
		return -1;
	}
	
	@Override
	public int getEnd()
	{
		return -1;
	}
	
	@Override
	public IToken next() throws SyntaxError
	{
		if (this.next == null)
		{
			throw new SyntaxError(this, "No next token!");
		}
		return this.next;
	}
	
	@Override
	public IToken prev() throws SyntaxError
	{
		if (this.prev == null)
		{
			throw new SyntaxError(this, "No prev token!");
		}
		return this.prev;
	}
	
	@Override
	public boolean match(Object object) throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public boolean match(Object... objects) throws SyntaxError
	{
		throw new SyntaxError(this, "No token!");
	}
	
	@Override
	public void setIndex(int index)
	{}
	
	@Override
	public void setPrev(IToken token)
	{
		this.prev = token;
	}
	
	@Override
	public void setNext(IToken token)
	{
		this.next = token;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next != null;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev != null;
	}
}
