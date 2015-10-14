package dyvil.tools.parsing.marker;

import dyvil.tools.parsing.position.ICodePosition;

public class Info extends Marker
{
	private static final long serialVersionUID = 8238564164743045522L;
	
	protected Info(ICodePosition position)
	{
		super(position);
	}
	
	public Info(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	@Override
	public String getMarkerType()
	{
		return "info";
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
