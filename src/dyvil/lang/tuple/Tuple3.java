package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple3<A, B, C>
{
	public A _1;
	public B _2;
	public C _3;
	
	public Tuple3(A a, B b, C c)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple3)
		{
			Tuple3 that = (Tuple3) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_1, _2, _3);
	}
	
	@Override
	public String toString()
	{
		return "(" + _1 + "," + _2 + "," + _3 + ")";
	}
}
