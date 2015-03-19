package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IToken extends ICodePosition
{
	public int type();
	
	public String text();
	
	public boolean equals(String value);
	
	public default String stringValue()
	{
		return null;
	}
	
	public default char charValue()
	{
		return 0;
	}
	
	public default int intValue()
	{
		return 0;
	}
	
	public default long longValue()
	{
		return 0L;
	}
	
	public default float floatValue()
	{
		return 0F;
	}
	
	public default double doubleValue()
	{
		return 0D;
	}
	
	public default boolean isInferred()
	{
		return false;
	}
	
	public IToken prev() throws SyntaxError;
	
	public IToken next() throws SyntaxError;
	
	public IToken getPrev();
	
	public IToken getNext();
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
	
	public boolean hasPrev();
	
	public boolean hasNext();
}
