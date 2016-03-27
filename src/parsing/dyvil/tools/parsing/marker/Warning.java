package dyvil.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.tools.parsing.position.ICodePosition;

public class Warning extends Marker
{
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
	public String getColor()
	{
		return Console.ANSI_YELLOW;
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
