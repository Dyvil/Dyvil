package dyvil.lang.tuple;

import java.util.Objects;

public class Tuple9<A, B, C, D, E, F, G, H, I>
{
	public A _1;
	public B _2;
	public C _3;
	public D _4;
	public E _5;
	public F _6;
	public G _7;
	public H _8;
	public I _9;
	
	public Tuple9(A a, B b, C c, D d, E e, F f, G g, H h, I i)
	{
		this._1 = a;
		this._2 = b;
		this._3 = c;
		this._4 = d;
		this._5 = e;
		this._6 = f;
		this._7 = g;
		this._8 = h;
		this._9 = i;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple9)
		{
			Tuple9 that = (Tuple9) obj;
			return this._1 == that._1 && this._2 == that._2 && this._3 == that._3 && this._4 == that._4 && this._5 == that._5 && this._6 == that._6 && this._7 == that._7 && this._8 == that._8 && this._9 == that._9;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9);
	}
}
