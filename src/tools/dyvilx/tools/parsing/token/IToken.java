package dyvilx.tools.parsing.token;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvil.lang.Name;

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

	@NonNull IToken prev();

	void setPrev(@NonNull IToken prev);

	@NonNull IToken next();

	void setNext(@NonNull IToken next);
}
