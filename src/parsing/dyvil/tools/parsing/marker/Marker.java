package dyvil.tools.parsing.marker;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.Console;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class Marker implements Comparable<Marker>
{
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
			this.info = new ArrayList<>(2);
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

	public void log(String code, StringBuilder buf, boolean colors)
	{
		final String type = this.getMarkerType();
		final String message = this.message;

		buf.append("line ").append(this.position.startLine()).append(": ");

		if (colors)
		{
			if (this.isError())
			{
				buf.append(Console.ANSI_RED);
			}
			else if (this.isWarning())
			{
				buf.append(Console.ANSI_YELLOW);
			}
			else
			{
				buf.append(Console.ANSI_BLUE);
			}
		}

		buf.append(type);
		if (message != null)
		{
			buf.append(": ").append(message);
		}

		if (colors)
		{
			buf.append(Console.ANSI_RESET);
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

		if (code == null)
		{
			buf.append('\n');
			return;
		}

		// Compute newline locations
		int startIndex = this.position.startIndex();
		int endIndex = this.position.endIndex();
		final int codeLength = code.length();

		if (startIndex >= codeLength)
		{
			startIndex = codeLength - 1;
		}
		if (endIndex > codeLength)
		{
			endIndex = codeLength;
		}

		final int prevNL = prevNL(code, startIndex);
		final int nextNL = nextNL(code, endIndex);

		// Append Line
		buf.append('\n');

		for (int i = prevNL; i < nextNL; i++)
		{
			final char c = code.charAt(i);
			if (c != '\n')
			{
				buf.append(c);
			}
		}

		buf.append('\n');

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

		if (colors)
		{
			buf.append(Console.ANSI_RED);
		}
		for (int i = startIndex; i < endIndex; i++)
		{
			buf.append('¯');
		}
		if (colors)
		{
			buf.append(Console.ANSI_RESET);
		}

		buf.append('\n');
	}

	private static int prevNL(String code, int start)
	{
		if (code.charAt(start) == '\n')
		{
			--start;
		}

		final int i = code.lastIndexOf('\n', start);
		if (i < 0)
		{
			return 0;
		}
		return i;
	}

	private static int nextNL(String code, int end)
	{
		final int i = code.indexOf('\n', end);
		if (i < 0)
		{
			return code.length();
		}
		return i;
	}
}
