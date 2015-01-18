package dyvil.tools.compiler.lexer.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Marker extends Exception
{
	private static final long	serialVersionUID	= 8313691845679541217L;
	
	private String				suggestion;
	private List<String>		info;
	
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
	
	public void setSuggestion(String suggestion)
	{
		this.suggestion = suggestion;
	}
	
	public String getSuggestion()
	{
		return this.suggestion;
	}
	
	public void addInfo(String info)
	{
		if (this.info == null)
		{
			this.info = new ArrayList(2);
		}
		this.info.add(info);
	}
	
	public abstract String getMarkerType();
	
	public void log(Logger logger)
	{
		StringBuilder buf = new StringBuilder();
		CodeFile file = this.position.getFile();
		String type = this.getMarkerType();
		String message = this.getMessage();
		String suggestion = this.getSuggestion();
		
		buf.append(file).append(':').append(this.position.getLineNumber()).append(": ");
		buf.append(type);
		if (message != null)
		{
			buf.append(": ").append(message);
		}
		if (suggestion != null)
		{
			buf.append(" - ").append(suggestion);
		}
		
		int prevNL = this.position.getPrevNewline();
		int nextNL = this.position.getNextNewline();
		String code = file.getCode();
		String line = code.substring(prevNL, nextNL);
		
		// Append Line
		buf.append('\n').append(line).append('\n');
		
		// Append ^
		for (int i = prevNL; i < this.position.getStart(); i++)
		{
			char c = code.charAt(i);
			if (c == '\t')
			{
				buf.append('\t');
			}
			else
			{
				buf.append(' ');
			}
		}
		buf.append('^');
		
		// Append Info (if any)
		if (this.info != null)
		{
			buf.append('\n');
			for (String s : this.info)
			{
				buf.append('\n').append(s);
			}
		}
		
		logger.info(buf.toString());
	}
}
