package dyvilx.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

@Deprecated
@dyvil.annotation.Deprecated(since = "v0.45.0", forRemoval = "v0.46.0")
public class InfoMarker extends Marker
{
	public InfoMarker(SourcePosition position, String message)
	{
		super(position, message);
	}

	@Override
	public MarkerLevel getLevel()
	{
		return MarkerLevel.INFO;
	}

	@Override
	public String getColor()
	{
		return Console.ANSI_CYAN;
	}

	@Override
	public boolean isError()
	{
		return false;
	}

	@Override
	public boolean isWarning()
	{
		return false;
	}
}
