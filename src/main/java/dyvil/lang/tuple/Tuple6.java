package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple6<A, B, C, D, E, F>
{
	public A _1;
	public B _2;
	public C _3;
	public D _4;
	public E _5;
	public F _6;
	
	public Tuple6(A a, B b, C c, D d, E e, F f)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
		this._4 = d;
		this._5 = e;
		this._6 = f;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple6)
		{
			Tuple6 that = (Tuple6) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3 && this._4 == that._4 && this._5 == that._5 && this._6 == that._6;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_1, _2, _3, _4, _5, _6);
	}
	
	@Override
	public String toString()
	{
		return "(" + _1 + "," + _2 + "," + _3 + "," + _4 + "," + _5 + "," + _6 + ")";
	}
}
