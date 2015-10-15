package dyvil.tools.parsing.token;

import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IToken extends ICodePosition
{
	public int type();
	
	public default Name nameValue()
	{
		return null;
	}
	
	public default String stringValue()
	{
		return null;
	}
	
	public default int intValue()
	{
		return 0;
	}
	
	public default long longValue()
	{
		return 0L;
	}
	
	public default void setLong(long value)
	{
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
	
	public IToken prev();
	
	public IToken next();
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
	
	public boolean hasPrev();
	
	public boolean hasNext();
}
