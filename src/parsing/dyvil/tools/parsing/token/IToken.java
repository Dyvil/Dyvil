package dyvil.tools.parsing.token;

import dyvil.source.position.SourcePosition;
import dyvil.tools.parsing.Name;

public interface IToken extends SourcePosition
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

	void setPrev(IToken prev);

	IToken next();

	void setNext(IToken next);
}
