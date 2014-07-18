package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple5<A, B, C, D, E>
{
	public A _1;
	public B _2;
	public C _3;
	public D _4;
	public E _5;
	
	public Tuple5(A a, B b, C c, D d, E e)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
		this._4 = d;
		this._5 = e;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple5)
		{
			Tuple5 that = (Tuple5) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3 && this._4 == that._4 && this._5 == that._5;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_1, _2, _3, _4, _5);
	}
	
	@Override
	public String toString()
	{
		return "(" + _1 + "," + _2 + "," + _3 + "," + _4 + "," + _5 + ")";
	}
}
