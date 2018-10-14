package dyvilx.tools.parsing.marker;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.io.AppendablePrintStream;
import dyvil.io.Console;
import dyvil.source.Source;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

import java.util.Arrays;
import java.util.Objects;

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

	public List<String> getInfo()
	{
		return this.info != null ? this.info : (this.info = new ArrayList<>(2));
	}

	public void addInfo(String info)
	{
		this.getInfo().add(info);
	}

	public void addError(Throwable throwable)
	{
		// TODO optimize / avoid unnecessary temp objects
		final StringBuilder builder = new StringBuilder();
		throwable.printStackTrace(new AppendablePrintStream(builder));
		this.getInfo().addAll(Arrays.asList(builder.toString().split("\n")));
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

	public boolean equals(@NonNull Marker that)
	{
		return this == that || //
		       this.position.equals(that.position) //
		       && this.getLevel() == that.getLevel() //
		       && this.message.equals(that.message) //
		       && Objects.equals(this.info, that.info);
	}

	// --------------- Hashing ---------------

	@Override
	public int hashCode()
	{
		int result = this.position.hashCode();
		result = 31 * result + this.getLevel().hashCode();
		result = 31 * result + this.message.hashCode();
		result = 31 * result + Objects.hashCode(this.info);
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
