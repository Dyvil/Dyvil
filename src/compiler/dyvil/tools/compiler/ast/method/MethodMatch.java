package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.util.Util;

public final class MethodMatch implements Comparable<MethodMatch>
{
	public final IMethod	method;
	public final float		match;
	
	public MethodMatch(IMethod method, float match)
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
		Util.methodSignatureToString(this.method, buf);
		return buf.toString();
	}
}
