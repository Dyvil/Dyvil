package dyvil.tools.compiler.lexer.marker;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SemanticError extends Marker
{
	private static final long serialVersionUID = -2234451954260010124L;
	
	public SemanticError(ICodePosition position)
	{
		super(position);
	}
	
	public SemanticError(ICodePosition position, String message)
	{
		super(position, message);
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
	
	@Override
	public boolean isWarning()
	{
		return false;
	}
}
