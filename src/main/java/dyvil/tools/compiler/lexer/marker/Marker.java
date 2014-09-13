package dyvil.tools.compiler.lexer.marker;

import java.io.PrintStream;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Marker extends Exception
{
	private static final long	serialVersionUID	= 8313691845679541217L;
	
	public String				suggestion;
	
	public ICodePosition			position;
	
	public Marker(ICodePosition position)
	{
		this(position, null, null);
	}
	
	public Marker(ICodePosition position, String message)
	{
		this(position, message, null);
	}
	
	public Marker(ICodePosition position, String message, String suggestion)
	{
		super(message);
		this.position = position;
		this.suggestion = suggestion;
	}
	
	public Marker(ICodePosition position, Throwable cause)
	{
		super(cause);
		this.position = position;
	}
	
	public String getSuggestion()
	{
		return this.suggestion;
	}
	
	public abstract void print(PrintStream out);
	
	protected void appendMessage(PrintStream out, StringBuilder builder)
	{
		String message = this.getMessage();
		String suggestion = this.getSuggestion();
		
		builder.append(" \"").append(this.position.getText()).append('"');
		if (message != null)
		{
			builder.append(": ").append(message);
		}
		if (suggestion != null)
		{
			builder.append(" - ").append(suggestion);
		}
		
		builder.append('\n').append(this.position.getCurrentLine()).append('\n');
		int prevNL = this.position.getPrevNewline();
		int nextNL = this.position.getNextNewline();
		
		for (int i = this.position.getStart() - prevNL - 1; i >= 0; i--)
		{
			builder.append(' ');
		}
		builder.append('^');
	}
}
