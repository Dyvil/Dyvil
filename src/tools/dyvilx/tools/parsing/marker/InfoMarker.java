package dyvilx.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.source.position.SourcePosition;

public class InfoMarker extends Marker
{
	public InfoMarker(SourcePosition position, String message)
	{
		super(position, message);
	}
	
	@Override
	public String getMarkerType()
	{
		return "info";
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
