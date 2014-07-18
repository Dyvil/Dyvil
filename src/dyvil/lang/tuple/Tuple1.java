package dyvil.lang.tuple;

import java.util.Objects;

public final class Tuple1<A>
{
	public A _1;
	
	public Tuple1(A a)
	{
		this._1 = a;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple1)
		{
			Tuple1 that = (Tuple1) obj;
			return this._1 == that._1;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this._1);
	}
}
