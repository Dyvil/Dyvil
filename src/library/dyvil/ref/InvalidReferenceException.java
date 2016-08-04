package dyvil.ref;

public class InvalidReferenceException extends IllegalStateException
{
	public InvalidReferenceException()
	{
	}

	public InvalidReferenceException(String message)
	{
		super(message);
	}

	public InvalidReferenceException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidReferenceException(Throwable cause)
	{
		super(cause);
	}
}
