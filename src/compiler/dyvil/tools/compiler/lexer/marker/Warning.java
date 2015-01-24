package dyvil.tools.compiler.lexer.marker;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Warning extends Marker
{
	private static final long	serialVersionUID	= 8238564164743045522L;
	
	protected Warning(ICodePosition position)
	{
		super(position);
	}
	
	protected Warning(ICodePosition position, String message)
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
}
