package dyvil.tools.parsing.marker;

import dyvil.tools.parsing.position.ICodePosition;

public class IgnoredMarker extends Marker
{
	private static final long serialVersionUID = 6339084541799017767L;
	
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
	public void log(String code, StringBuilder buf)
	{
	}
}
