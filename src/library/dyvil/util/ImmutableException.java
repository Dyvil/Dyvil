package dyvil.util;

/**
 * An {@linkplain Exception} that is thrown when accessing a mutating member of
 * an immutable collection.
 * 
 * @author Clashsoft
 */
public class ImmutableException extends RuntimeException
{
	private static final long serialVersionUID = -5032047108183334569L;
	
	public ImmutableException()
	{
	}
	
	public ImmutableException(String message)
	{
		super(message);
	}
}
