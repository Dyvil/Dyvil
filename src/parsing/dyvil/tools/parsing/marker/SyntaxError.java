package dyvil.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.tools.parsing.position.ICodePosition;

public class SyntaxError extends Marker
{
	public SyntaxError(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	@Override
	public String getMarkerType()
	{
		return "syntax";
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
