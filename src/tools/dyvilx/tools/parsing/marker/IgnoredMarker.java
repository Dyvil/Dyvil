package dyvilx.tools.parsing.marker;

import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

public class IgnoredMarker extends Marker
{
	public static final IgnoredMarker instance = new IgnoredMarker();

	private IgnoredMarker()
	{
		super(SourcePosition.ORIGIN, "");
	}

	@Override
	public void addInfo(String info)
	{
	}

	@Override
	public MarkerLevel getLevel()
	{
		return MarkerLevel.IGNORE;
	}

	@Override
	public String getColor()
	{
		return "";
	}

	@Override
	public boolean isIgnored()
	{
		return true;
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
