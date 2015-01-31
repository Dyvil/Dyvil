package dyvil.lang;

public class MatchError extends RuntimeException
{
	private static final long	serialVersionUID	= 2882649299151786454L;
	
	private Object				match;
	
	public MatchError(Object match)
	{
		this.match = match;
	}
	
	@Override
	public String getMessage()
	{
		if (this.match == null)
		{
			return "null";
		}
		else
		{
			try
			{
				return this.match.toString() + " (of class " + this.match.getClass().getName() + ")";
			}
			catch (Throwable t)
			{
				return "An instance of class " + this.match.getClass().getName();
			}
		}
	}
}
