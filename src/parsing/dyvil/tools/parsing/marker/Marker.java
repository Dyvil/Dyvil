package dyvil.tools.parsing.marker;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class Marker implements Comparable<Marker>
{
	private static final long serialVersionUID = 8313691845679541217L;
	
	protected ICodePosition position;
	
	private String       message;
	private List<String> info;
	
	protected Marker()
	{
	}
	
	public Marker(ICodePosition position)
	{
		if (position == null)
		{
			throw new IllegalArgumentException("Marker Position cannot be null");
		}
		
		this.position = position;
	}
	
	public Marker(ICodePosition position, String message)
	{
		if (position == null)
		{
			throw new IllegalArgumentException("Marker Position cannot be null");
		}
		
		this.message = message;
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
	
	public abstract boolean isWarning();
	
	@Override
	public int compareTo(Marker o)
	{
		int start1 = this.position.startIndex();
		int start2 = o.position.startIndex();
		return start1 == start2 ? 0 : start1 < start2 ? -1 : 0;
	}
	
	public void log(String code, StringBuilder buf)
	{
		String type = this.getMarkerType();
		String message = this.message;
		
		buf.append("line ").append(this.position.startLine()).append(": ").append(type);
		if (message != null)
		{
			buf.append(": ").append(message);
		}
		
		int startIndex = this.position.startIndex();
		int endIndex = this.position.endIndex();
		int codeLength = code.length();
		if (startIndex >= codeLength)
		{
			startIndex = codeLength - 1;
		}
		if (endIndex >= codeLength)
		{
			endIndex = codeLength - 1;
		}
		
		int prevNL = prevNL(code, startIndex);
		int nextNL = nextNL(code, endIndex);
		String line = code.substring(prevNL, nextNL);
		
		// Append Line
		buf.append('\n').append(line).append('\n');
		
		// Append ^
		for (int i = prevNL; i < startIndex; i++)
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
		if (code.charAt(start) == '\n')
		{
			start--;
		}
		int i = code.lastIndexOf('\n', start);
		if (i < 0)
		{
			return 0;
		}
		return i;
	}
	
	private static int nextNL(String code, int end)
	{
		int i = code.indexOf('\n', end);
		if (i < 0)
		{
			return code.length() - 1;
		}
		return i;
	}
}
