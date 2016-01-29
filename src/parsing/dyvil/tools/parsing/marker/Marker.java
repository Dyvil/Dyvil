package dyvil.tools.parsing.marker;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class Marker implements Comparable<Marker>
{
	private static final long serialVersionUID = 8313691845679541217L;
	
	protected final ICodePosition position;
	
	private final String       message;
	private       List<String> info;
	
	public Marker(ICodePosition position, String message)
	{
		if (position == null)
		{
			position = ICodePosition.ORIGIN;
		}
		
		this.message = message;
		this.position = position;
	}

	public ICodePosition getPosition()
	{
		return this.position;
	}

	public String getMessage()
	{
		return this.message;
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
		final String type = this.getMarkerType();
		final String message = this.message;
		
		buf.append("line ").append(this.position.startLine()).append(": ").append(type);
		if (message != null)
		{
			buf.append(": ").append(message);
		}

		// Append Info (if any)
		if (this.info != null)
		{
			for (String s : this.info)
			{
				buf.append("\n\t").append(s);
			}
			buf.append('\n');
		}

		// Compute newline locations
		int startIndex = this.position.startIndex();
		int endIndex = this.position.endIndex();
		final int codeLength = code.length();
		if (startIndex >= codeLength)
		{
			startIndex = codeLength - 1;
		}
		if (endIndex >= codeLength)
		{
			endIndex = codeLength - 1;
		}
		
		final int prevNL = prevNL(code, startIndex);
		final int nextNL = nextNL(code, endIndex);
		final String line = code.substring(prevNL, nextNL);
		
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
		for (int i = startIndex; i < endIndex; i++)
		{
			buf.append('¯');
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
