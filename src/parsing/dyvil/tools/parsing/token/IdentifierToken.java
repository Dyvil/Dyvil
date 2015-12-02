package dyvil.tools.parsing.token;

import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public class IdentifierToken implements IToken
{
	public IToken prev;
	public IToken next;
	
	public final int  type;
	public final Name name;
	
	public final int lineNumber;
	public final int start;
	public final int end;
	
	public IdentifierToken(IToken prev, Name name, int type, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		this.name = name;
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public IdentifierToken(Name name, int type, int lineNumber, int start, int end)
	{
		this.name = name;
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int type()
	{
		return this.type;
	}
	
	@Override
	public Name nameValue()
	{
		return this.name;
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
	public IToken prev()
	{
		return this.prev;
	}
	
	@Override
	public IToken next()
	{
		return this.next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next.type() != 0;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev.type() != 0;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.end);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Identifier '" + this.name + "\'";
	}
}
