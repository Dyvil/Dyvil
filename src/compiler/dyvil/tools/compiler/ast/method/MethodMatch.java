package dyvil.tools.compiler.ast.method;

public class MethodMatch implements Comparable<MethodMatch>
{
	public IMethod	theMethod;
	public int		match;
	
	public MethodMatch(IMethod theMethod, int match)
	{
		this.theMethod = theMethod;
		this.match = match;
	}
	
	@Override
	public int compareTo(MethodMatch o)
	{
		return this.match < o.match ? 1 : this.match == o.match ? 0 : -1;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.theMethod.toString("", buf);
		return buf.toString();
	}
}
