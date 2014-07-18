package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple2<A, B>
{
	public A _1;
	public B _2;
	
	public Tuple2(A a, B b)
	{
		this._1 = a;
		this._2 = b;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple2)
		{
			Tuple2 that = (Tuple2) obj;
			return this._1 == that._1 && this._2 == that._2;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_1, _2);
	}
	
	@Override
	public String toString()
	{
		return "(" + _1 + "," + _2 + ")";
	}
}
