package dyvil.tools.parsing.marker;

import dyvil.tools.parsing.position.ICodePosition;

public class SyntaxError extends Marker
{
	private static final long serialVersionUID = -2234451954260010124L;
	
	public SyntaxError(ICodePosition position)
	{
		super(position);
	}
	
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
