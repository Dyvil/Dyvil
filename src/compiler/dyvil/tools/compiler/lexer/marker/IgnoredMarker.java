package dyvil.tools.compiler.lexer.marker;

public class IgnoredMarker extends Marker
{
	private static final long			serialVersionUID	= 6339084541799017767L;
	
	public static final IgnoredMarker	instance			= new IgnoredMarker();
	
	private IgnoredMarker()
	{
	}
	
	@Override
	public void addInfo(String info)
	{
	}
	
	@Override
	public String getMarkerType()
	{
		return "ignored";
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
	
	@Override
	public void log(String code, StringBuilder buf)
	{
	}
}
