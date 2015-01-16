package dyvil.tools.compiler.lexer.marker;

import java.util.logging.Logger;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SemanticError extends Marker
{
	private static final long	serialVersionUID	= -2234451954260010124L;
	
	public SemanticError(ICodePosition position)
	{
		super(position);
	}
	
	public SemanticError(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	public SemanticError(ICodePosition position, String message, String suggestion)
	{
		super(position, message, suggestion);
	}
	
	public SemanticError(ICodePosition position, Throwable cause)
	{
		super(position, cause);
	}
	
	@Override
	public void log(Logger logger)
	{
		StringBuilder builder = new StringBuilder("Semantic error at Token");
		this.appendMessage(builder);
		logger.info(builder.toString());
	}
}
