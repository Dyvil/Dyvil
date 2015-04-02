package dyvil.tools.compiler.ast.method;

public final class ConstructorMatch implements Comparable<ConstructorMatch>
{
	public final IConstructor	constructor;
	public final int			match;
	
	public ConstructorMatch(IConstructor constructor, int match)
	{
		this.constructor = constructor;
		this.match = match;
	}
	
	@Override
	public int compareTo(ConstructorMatch o)
	{
		return this.match < o.match ? 1 : this.match == o.match ? 0 : -1;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.constructor.toString("", buf);
		return buf.toString();
	}
}
