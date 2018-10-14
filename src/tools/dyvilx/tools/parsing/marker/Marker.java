package dyvilx.tools.parsing.marker;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.AppendablePrintStream;
import dyvil.io.Console;
import dyvil.source.Source;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

public abstract class Marker implements Comparable<Marker>
{
	// =============== Fields ===============

	protected final SourcePosition position;

	private final String       message;
	private       List<String> info;

	// =============== Constructors ===============

	public Marker(SourcePosition position, String message)
	{
		if (position == null)
		{
			position = SourcePosition.ORIGIN;
		}

		this.message = message;
		this.position = position;
	}

	// =============== Properties ===============

	public SourcePosition getPosition()
	{
		return this.position;
	}

	public String getMessage()
	{
		return this.message;
	}

	public abstract MarkerLevel getLevel();

	public abstract String getColor();

	public boolean isIgnored()
	{
		return false;
	}

	public abstract boolean isError();

	public abstract boolean isWarning();

	// =============== Methods ===============

	// --------------- Info ---------------

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

	// --------------- Comparison ---------------

	@Override
	public int compareTo(@NonNull Marker o)
	{
		final int byPos = this.position.compareTo(o.position);
		if (byPos != 0)
		{
			return byPos;
		}
		final int byLevel = -this.getLevel().compareTo(o.getLevel());
		if (byLevel != 0)
		{
			return byLevel;
		}
		return this.message.compareTo(o.message);
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj || obj instanceof Marker && this.equals((Marker) obj);
	}

	public boolean equals(Marker that)
	{
		if (this == that)
		{
			return true;
		}
		if (!this.position.equals(that.position))
		{
			return false;
		}
		if (this.getLevel() != that.getLevel())
		{
			return false;
		}
		if (!this.message.equals(that.message))
		{
			return false;
		}
		//
		return this.info != null ? this.info.equals(that.info) : that.info == null;
	}

	// --------------- Hashing ---------------

	@Override
	public int hashCode()
	{
		int result = this.position.hashCode();
		result = 31 * result + (this.getLevel().hashCode());
		result = 31 * result + (this.message != null ? this.message.hashCode() : 0);
		result = 31 * result + (this.info != null ? this.info.hashCode() : 0);
		return result;
	}

	// --------------- Formatting ---------------

	public final void log(Source source, String indent, StringBuilder buffer, boolean colors)
	{
		final String type = BaseMarkers.INSTANCE.getString("marker_level." + this.getLevel().name().toLowerCase());

		final SourcePosition position = this.position;
		final int endLine = position.endLine();
		final String colorString = colors ? this.getColor() : "";

		buffer.append(indent);

		this.appendLine(buffer, source.line(endLine), position.startColumn(), position.endColumn(), colors,
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

		buffer.append(indent).append('\n');
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

		if (colors)
		{
			buf.append(colorString);
		}
		for (int i = startColumn; i < endColumn; i++)
		{
			buf.append('^');
		}
		if (startColumn == endColumn)
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
