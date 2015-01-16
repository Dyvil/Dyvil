package dyvil.tools.compiler.lexer.marker;

import java.util.logging.Logger;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SyntaxError extends Marker
{
	private static final long	serialVersionUID	= -2234451954260010124L;
	
	public SyntaxError(ICodePosition position)
	{
		super(position);
	}
	
	public SyntaxError(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	public SyntaxError(ICodePosition position, String message, String suggestion)
	{
		super(position, message, suggestion);
	}
	
	public SyntaxError(ICodePosition position, Throwable cause)
	{
		super(position, cause);
	}
	
	@Override
	public void log(Logger logger)
	{
		StringBuilder builder = new StringBuilder("Syntax error at Token");
		this.appendMessage(builder);
		logger.info(builder.toString());
	}
}
