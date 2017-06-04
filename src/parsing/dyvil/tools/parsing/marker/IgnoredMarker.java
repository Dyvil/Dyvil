package dyvil.tools.parsing.marker;

import dyvil.source.position.SourcePosition;
import dyvil.tools.parsing.source.Source;

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
	public String getMarkerType()
	{
		return "ignored";
	}

	@Override
	public String getColor()
	{
		return "";
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
	
	@Override
	public void log(Source source, String indent, StringBuilder buffer, boolean colors)
	{
	}
}
