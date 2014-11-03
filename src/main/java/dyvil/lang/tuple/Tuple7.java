package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple7<A, B, C, D, E, F, G>
{
	public A	_1;
	public B	_2;
	public C	_3;
	public D	_4;
	public E	_5;
	public F	_6;
	public G	_7;
	
	public Tuple7(A a, B b, C c, D d, E e, F f, G g)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
		this._4 = d;
		this._5 = e;
		this._6 = f;
		this._7 = g;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple7)
		{
			Tuple7 that = (Tuple7) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3 && this._4 == that._4 && this._5 == that._5 && this._6 == that._6 && this._7 == that._7;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this._1, this._2, this._3, this._4, this._5, this._6, this._7);
	}
	
	@Override
	public String toString()
	{
		return "(" + this._1 + "," + this._2 + "," + this._3 + "," + this._4 + "," + this._5 + "," + this._6 + "," + this._7 + ")";
	}
}
