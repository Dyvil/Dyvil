package dyvil.tools.compiler.lexer.marker;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Warning extends Marker
{
	private static final long	serialVersionUID	= 8238564164743045522L;
	
	public Warning(ICodePosition position)
	{
		super(position);
	}
	
	public Warning(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	public Warning(ICodePosition position, String message, String suggestion)
	{
		super(position, message, suggestion);
	}
	
	public Warning(ICodePosition position, Throwable cause)
	{
		super(position, cause);
	}
	
	@Override
	public String getMarkerType()
	{
		return "warning";
	}
}
