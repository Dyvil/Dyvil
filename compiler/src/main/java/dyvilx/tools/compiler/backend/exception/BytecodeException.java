package dyvilx.tools.compiler.backend.exception;

public class BytecodeException extends RuntimeException
{
	private static final long serialVersionUID = 1678495524827729085L;
	
	public BytecodeException()
	{
		super();
	}
	
	public BytecodeException(String message)
	{
		super(message);
	}
	
	public BytecodeException(Throwable cause)
	{
		super(cause);
	}
}
