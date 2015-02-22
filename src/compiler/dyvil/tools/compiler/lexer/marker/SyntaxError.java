package dyvil.tools.compiler.lexer.marker;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SyntaxError extends Marker
{
	private static final long	serialVersionUID	= -2234451954260010124L;
	
	public boolean				reparse;
	
	public SyntaxError(ICodePosition position)
	{
		super(position);
	}
	
	public SyntaxError(ICodePosition position, String message)
	{
		super(position, message);
	}
	
	public SyntaxError(ICodePosition position, String message, boolean reparse)
	{
		super(position, message);
		this.reparse = reparse;
	}
	
	@Override
	public String getMarkerType()
	{
		return "error";
	}
	
	@Override
	public boolean isError()
	{
		return true;
	}
}
