package dyvil.tools.parsing.marker;

import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.source.Source;

public class IgnoredMarker extends Marker
{
	public static final IgnoredMarker instance = new IgnoredMarker();
	
	private IgnoredMarker()
	{
		super(ICodePosition.ORIGIN, "");
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
	public void log(Source source, StringBuilder buf, boolean colors)
	{
	}
}
