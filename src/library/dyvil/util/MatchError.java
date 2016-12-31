package dyvil.util;

import dyvil.annotation.internal.NonNull;

/**
 * A <b>MatchError</b> is a {@link RuntimeException} thrown when a non-exhaustive {@code match} expression receives a
 * value that none of it's patterns can handle. The MatchError contains the value that was attempted to be matched, and
 * it's {@link #getMessage()} method returns a string describing the value as well as it's type.
 *
 * @author Clashsoft
 * @version 1.0
 */
public class MatchError extends RuntimeException
{
	private static final long serialVersionUID = 2882649299151786454L;

	private Object match;

	public MatchError(Object match)
	{
		this.match = match;
	}

	@NonNull
	@Override
	public String getMessage()
	{
		if (this.match == null)
		{
			return "null";
		}
		try
		{
			return this.match.toString() + " (of class " + this.match.getClass().getName() + ")";
		}
		catch (Throwable t)
		{
			return "An instance of class " + this.match.getClass().getCanonicalName();
		}
	}
}
