package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Tokens;

public final class FloatToken implements IToken
{
	private IToken		prev;
	private IToken		next;
	
	private final int	lineNumber;
	private final int	start;
	private final int	end;
	
	private float			value;
	
	public FloatToken(IToken prev, float value, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public FloatToken(float value, int lineNumber, int start, int end)
	{
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String text()
	{
		return null;
	}
	
	@Override
	public int type()
	{
		return Tokens.FLOAT;
	}
	
	@Override
	public boolean equals(String value)
	{
		return false;
	}
	
	@Override
	public float floatValue()
	{
		return this.value;
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
		return this.lineNumber;
	}
	
	@Override
	public int endLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}
	
	@Override
	public void setNext(IToken next)
	{
		this.next = next;
	}
	
	@Override
	public IToken prev() throws SyntaxError
	{
		if (this.prev == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.prev;
	}
	
	@Override
	public IToken next() throws SyntaxError
	{
		if (this.next == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.next;
	}
	
	@Override
	public IToken getPrev()
	{
		return this.prev;
	}
	
	@Override
	public IToken getNext()
	{
		return this.next;
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
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.endIndex());
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Float " + this.value + " (line " + this.lineNumber + ")";
	}
}
