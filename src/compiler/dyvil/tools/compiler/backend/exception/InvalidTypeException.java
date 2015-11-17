package dyvil.tools.compiler.backend.exception;

import dyvil.reflect.Opcodes;

public class InvalidTypeException extends BytecodeException
{
	private static final long serialVersionUID = -3066382947566741703L;
	
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
	
	public InvalidTypeException(int opcode, String location, String expected, String actual)
	{
		super("Invalid Type on " + Opcodes.toString(opcode) + " Instruction at " + location + ": Expected: " + expected + ", Actual: " + actual);
	}
}
