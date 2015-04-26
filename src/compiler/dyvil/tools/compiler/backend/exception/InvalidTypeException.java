package dyvil.tools.compiler.backend.exception;

public class InvalidTypeException extends BytecodeException
{
	private static final long	serialVersionUID	= -3066382947566741703L;
	
	public InvalidTypeException()
	{
		super();
	}
	
	public InvalidTypeException(String message)
	{
		super(message);
	}
	
	public InvalidTypeException(Throwable cause)
	{
		super(cause);
	}
	
	public InvalidTypeException(String opcode, String expected, String actual)
	{
		super("Invalid Type on " + opcode + " Instruction: Expected: " + expected + ", Actual: " + actual);
	}
}
