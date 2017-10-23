package dyvilx.tools.parsing.marker;

import dyvil.io.Console;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

public class SemanticError extends Marker
{
	public SemanticError(SourcePosition position, String message)
	{
		super(position, message);
	}

	@Override
	public MarkerLevel getLevel()
	{
		return MarkerLevel.ERROR;
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
