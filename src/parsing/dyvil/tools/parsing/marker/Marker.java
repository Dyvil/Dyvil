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

	public void log(Source source, String indent, StringBuilder buffer, boolean colors)
	{
		final String type = this.getMarkerType();

		final ICodePosition position = this.position;
		final int endLine = position.endLine();
		final String colorString = colors ? this.getColor() : "";

		buffer.append(indent);

		this.appendLine(buffer, source.getLine(endLine), position.startColumn(), position.endColumn(), colors,
		                colorString);

		buffer.append(indent).append(colorString).append(type);
		if (this.message != null)
		{
			buffer.append(": ").append(this.message);
		}

		if (colors)
		{
			buffer.append(Console.ANSI_RESET);
		}

		buffer.append('\n');

		// Append Info (if any)
		if (this.info != null)
		{
			for (String s : this.info)
			{
				buffer.append(indent).append('\t').append(s).append('\n');
			}
		}
	}

	private void appendLine(StringBuilder buf, String line, int startColumn, int endColumn, boolean colors,
		                       String colorString)
	{
		final int limit = Math.min(startColumn, line.length());

		// Append Spaces
		for (int i = 0; i < limit; i++)
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
				buf.append('^');
			}
			if (colors)
			{
				buf.append(Console.ANSI_RESET);
			}

			buf.append('\n');
		}
	}
}
