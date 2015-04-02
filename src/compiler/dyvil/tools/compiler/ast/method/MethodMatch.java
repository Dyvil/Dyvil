package dyvil.tools.compiler.ast.method;

public final class MethodMatch implements Comparable<MethodMatch>
{
	public final IMethod	method;
	public final int		match;
	
	public MethodMatch(IMethod method, int match)
	{
		this.method = method;
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
		this.method.toString("", buf);
		return buf.toString();
	}
}
