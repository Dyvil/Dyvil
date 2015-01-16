package dyvil.tools.compiler.lexer.marker;

import java.util.logging.Logger;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Marker extends Exception
{
	private static final long	serialVersionUID	= 8313691845679541217L;
	
	public String				suggestion;
	
	public ICodePosition		position;
	
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
		if (position == null)
		{
			throw new IllegalArgumentException("Marker Position cannot be null");
		}
		
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
	
	public abstract void log(Logger logger);
	
	protected void appendMessage(StringBuilder builder)
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
		
		String code = this.position.getFile().getCode();
		int prevNL = this.position.getPrevNewline();
		int nextNL = this.position.getNextNewline();
		String line = code.substring(prevNL, nextNL);
		
		builder.append('\n').append(line).append('\n');
		
		for (int i = prevNL; i < this.position.getStart(); i++)
		{
			char c = code.charAt(i);
			if (c == '\t')
			{
				builder.append('\t');
			}
			else
			{
				builder.append(' ');
			}
		}
		builder.append('^');
	}
}
