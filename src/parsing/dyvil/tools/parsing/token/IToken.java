package dyvil.tools.parsing.token;

import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IToken extends ICodePosition
{
	int type();
	
	default Name nameValue()
	{
		return null;
	}
	
	default String stringValue()
	{
		return null;
	}
	
	default int intValue()
	{
		return 0;
	}
	
	default long longValue()
	{
		return 0L;
	}
	
	default float floatValue()
	{
		return 0F;
	}
	
	default double doubleValue()
	{
		return 0D;
	}
	
	default boolean isInferred()
	{
		return false;
	}
	
	IToken prev();
	
	IToken next();
	
	void setPrev(IToken prev);
	
	void setNext(IToken next);
	
	boolean hasPrev();
	
	boolean hasNext();
}
