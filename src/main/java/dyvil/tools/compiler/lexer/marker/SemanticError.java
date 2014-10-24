package dyvil.tools.compiler.lexer.marker;

import java.io.PrintStream;

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
	public void print(PrintStream out)
	{
		StringBuilder builder = new StringBuilder("Semantic error at Token");
		this.appendMessage(out, builder);
		out.println(builder);
	}
}
