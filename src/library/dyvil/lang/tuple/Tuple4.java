package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple4<A, B, C, D>
{
	public A	_1;
	public B	_2;
	public C	_3;
	public D	_4;
	
	public Tuple4(A a, B b, C c, D d)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
		this._4 = d;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple4)
		{
			Tuple4 that = (Tuple4) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3 && this._4 == that._4;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this._1, this._2, this._3, this._4);
	}
	
	@Override
	public String toString()
	{
		return "(" + this._1 + "," + this._2 + "," + this._3 + "," + this._4 + ")";
	}
}
