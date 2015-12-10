package dyvil.tools.compiler.backend.exception;

public class StackUnderflowException extends BytecodeException
{
	private static final long serialVersionUID = 2460012467001065283L;
	
	public StackUnderflowException()
	{
		super();
	}
	
	public StackUnderflowException(String message)
	{
		super(message);
	}
	
	public StackUnderflowException(Throwable cause)
	{
		super(cause);
	}
}
