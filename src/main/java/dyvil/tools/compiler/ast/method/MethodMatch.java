package dyvil.tools.compiler.ast.method;

public class MethodMatch implements Comparable<MethodMatch>
{
	public IMethod theMethod;
	public int match;
	
	public MethodMatch(IMethod theMethod, int match)
	{
		this.theMethod = theMethod;
		this.match = match;
	}
	
	@Override
	public int compareTo(MethodMatch o)
	{
		return Integer.compare(this.match, o.match);
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.theMethod.toString("", buf);
		return buf.toString();
	}
}
