package dyvil.tools.compiler.lexer.token;

import java.util.Objects;

import dyvil.tools.compiler.lexer.SyntaxError;

public class Token implements IToken
{
	private IToken			prev;
	private IToken			next;
	
	private int				index;
	
	private final int		type;
	private final String	value;
	private final Object	object;
	
	private final int		lineNumber;
	private final int		start;
	private final int		end;
	
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
	public Object object() throws SyntaxError
	{
		return this.object;
	}
	
	@Override
	public int type() throws SyntaxError
	{
		return this.type;
	}
	
	@Override
	public boolean equals(String value)
	{
		return Objects.equals(this.value, value);
	}
	
	@Override
	public boolean isType(int type) throws SyntaxError
	{
		return (this.type & type) == type;
	}
	
	@Override
	public int index()
	{
		return this.index;
	}
	
	@Override
	public int line() throws SyntaxError
	{
		return this.lineNumber;
	}
	
	@Override
	public int start()
	{
		return this.start;
	}
	
	@Override
	public int end()
	{
		return this.end;
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
		else if (object instanceof Number)
		{
			return this.isType(((Number) object).byteValue());
		}
		return this.equals(object);
	}
	
	@Override
	public boolean match(Object... objects) throws SyntaxError
	{
		IToken token = this;
		for (Object object : objects)
		{
			if (!token.match(objects))
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
	public String toString()
	{
		return "Token #" + this.index + ": \"" + this.value + "\" (line " + this.lineNumber + ")";
	}
}
