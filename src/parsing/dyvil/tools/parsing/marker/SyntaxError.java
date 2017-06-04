package dyvil.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.source.position.SourcePosition;

public class SyntaxError extends Marker
{
	public SyntaxError(SourcePosition position, String message)
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
