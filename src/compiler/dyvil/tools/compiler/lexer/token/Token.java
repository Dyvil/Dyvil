package dyvil.tools.compiler.lexer.token;

import java.util.Objects;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Token implements IToken
{
	public IToken			prev;
	public IToken			next;
	
	public int				index;
	
	public final int		type;
	public final String		value;
	public final Object		object;
	
	public final int		lineNumber;
	public final int		start;
	public final int		end;
	
	public Token(int index, String value, int type, Object object, int lineNumber, int start, int end)
	{
		this.index = index;
		this.value = value;
		this.type = type;
		this.object = object;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String value()
	{
		return this.value;
	}
	
	@Override
	public Object object()
	{
		return this.object;
	}
	
	@Override
	public int type()
	{
		return this.type;
	}
	
	@Override
	public boolean equals(String value)
	{
		return Objects.equals(this.value, value);
	}
	
	@Override
	public int index()
	{
		return this.index;
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
	public IToken prev()
	{
		if (this.prev == null)
		{
			this.prev = new FakeToken();
			this.prev.setNext(this);
		}
		return this.prev;
	}
	
	@Override
	public IToken next()
	{
		if (this.next == null)
		{
			this.next = new FakeToken();
			this.next.setPrev(this);
		}
		return this.next;
	}
	
	@Override
	public boolean match(Object object) throws SyntaxError
	{
		if (object instanceof String)
		{
			return this.equals((String) object);
		}
		return this.equals(object);
	}
	
	@Override
	public boolean match(Object... objects) throws SyntaxError
	{
		IToken token = this;
		for (Object object : objects)
		{
			if (!token.match(object))
			{
				return false;
			}
			token = token.next();
		}
		return true;
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
	public void setIndex(int index)
	{
		this.index = index;
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
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.end);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endLine());
	}
	
	@Override
	public String toString()
	{
		return "Token #" + this.index + ": \"" + this.value + "\" (line " + this.lineNumber + ")";
	}
}
