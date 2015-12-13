package dyvil.tools.parsing.marker;

import dyvil.tools.parsing.position.ICodePosition;

public class Warning extends Marker
{
	private static final long serialVersionUID = 8238564164743045522L;
	
	public Warning(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	@Override
	public String getMarkerType()
	{
		return "warning";
	}
	
	@Override
	public boolean isError()
	{
		return false;
	}
	
	@Override
	public boolean isWarning()
	{
		return true;
	}
}
