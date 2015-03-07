package dyvil.tools.compiler.lexer.marker;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Marker extends Exception
{
	private static final long	serialVersionUID	= 8313691845679541217L;
	
	private List<String>		info;
	
	public ICodePosition		position;
	
	public Marker(ICodePosition position)
	{
		super();
		if (position == null)
		{
			throw new IllegalArgumentException("Marker Position cannot be null");
		}
		
		this.position = position;
	}
	
	public Marker(ICodePosition position, String message)
	{
		super(message);
		if (position == null)
		{
			throw new IllegalArgumentException("Marker Position cannot be null");
		}
		
		this.position = position;
	}
	
	public Marker(ICodePosition position, Throwable cause)
	{
		super(cause);
		this.position = position;
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
	
	public abstract boolean isError();
	
	public void log(String code, StringBuilder buf)
	{
		String type = this.getMarkerType();
		String message = this.getMessage();
		
		buf.append("line ").append(this.position.startLine()).append(": ").append(type);
		if (message != null)
		{
			buf.append(": ").append(message);
		}
		
		int prevNL = prevNL(code, this.position.startIndex());
		int nextNL = nextNL(code, this.position.endIndex());
		String line = code.substring(prevNL, nextNL);
		
		// Append Line
		buf.append('\n').append(line).append('\n');
		
		// Append ^
		for (int i = prevNL; i < this.position.startIndex(); i++)
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
			for (String s : this.info)
			{
				buf.append("\n\t").append(s);
			}
			buf.append('\n');
		}
		buf.append('\n');
	}
	
	private static int prevNL(String code, int start)
	{
		int i = code.lastIndexOf('\n', start);
		if (i < 0)
		{
			return 0;
		}
		return i + 1;
	}
	
	private static int nextNL(String code, int end)
	{
		int i = code.indexOf('\n', end);
		if (i < 0)
		{
			return code.length();
		}
		return i + 1;
	}
}
