package dyvilx.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

@Deprecated
@dyvil.annotation.Deprecated(since = "v0.45.0", forRemoval = "v0.46.0")
public class SyntaxError extends Marker
{
	public SyntaxError(SourcePosition position, String message)
	{
		super(position, message);
	}

	@Override
	public MarkerLevel getLevel()
	{
		return MarkerLevel.SYNTAX;
	}

	@Override
	public String getColor()
	{
		return Console.ANSI_RED;
	}

	@Override
	public boolean isError()
	{
		return true;
	}

	@Override
	public boolean isWarning()
	{
		return false;
	}
}
