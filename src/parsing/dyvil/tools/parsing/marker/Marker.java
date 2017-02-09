package dyvil.tools.parsing.marker;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.AppendablePrintStream;
import dyvil.io.Console;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.source.Source;

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

	public void addError(Throwable throwable)
	{
		final StringBuilder builder = new StringBuilder();
		throwable.printStackTrace(new AppendablePrintStream(builder));
		this.addInfo(builder.toString());
	}

	public abstract String getMarkerType();

	public abstract String getColor();

	public abstract boolean isError();

	public abstract boolean isWarning();

	@Override
	public int compareTo(@NonNull Marker o)
	{
		return this.position.compareTo(o.position);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof Marker))
		{
			return false;
		}

		final Marker marker = (Marker) obj;

		if (!this.position.equals(marker.position))
		{
			return false;
		}
		if (this.message != null ? !this.message.equals(marker.message) : marker.message != null)
		{
			return false;
		}
		//
		return this.info != null ? this.info.equals(marker.info) : marker.info == null;
	}

	@Override
	public int hashCode()
	{
		int result = this.position.hashCode();
		result = 31 * result + (this.message != null ? this.message.hashCode() : 0);
		result = 31 * result + (this.info != null ? this.info.hashCode() : 0);
		return result;
	}

	public void log(Source source, StringBuilder buf, boolean colors)
	{
		final String type = this.getMarkerType();
		final String message = this.message;

		final ICodePosition position = this.position;
		final int startLine = position.startLine();
		final int endLine = position.endLine();

		buf.append("line ").append(startLine).append(": ");

		final String colorString;
		if (colors)
		{
			colorString = this.getColor();
			buf.append(colorString);
		}
		else
		{
			colorString = null;
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
		buf.append('\n');

		if (startLine == endLine)
		{
			this.appendLine(buf, source.getLine(startLine), position.startColumn(), position.endColumn(), colors, colorString);
		}
		else
		{
			final String line = source.getLine(startLine);
			this.appendLine(buf, line, position.startColumn(), line.length(), colors, colorString);
			if (endLine - startLine > 1)
			{
				buf.append("\t...\n");
			}
			this.appendLine(buf, source.getLine(endLine), 0, position.endColumn(), colors, colorString);
		}
	}

	private void appendLine(StringBuilder buf, String line, int startColumn, int endColumn,
		                       boolean colors, String colorString)
	{
		buf.append(line);
		buf.append('\n');

		// Append Spaces
		for (int i = 0; i < startColumn; i++)
		{
			if (line.charAt(i) == '\t')
			{
				buf.append('\t');
			}
			else
			{
				buf.append(' ');
			}
		}

		if (startColumn < endColumn)
		{
			if (colors)
			{
				buf.append(colorString);
			}
			for (int i = startColumn; i < endColumn; i++)
			{
				buf.append('¯');
			}
			if (colors)
			{
				buf.append(Console.ANSI_RESET);
			}

			buf.append('\n');
		}
	}
}
