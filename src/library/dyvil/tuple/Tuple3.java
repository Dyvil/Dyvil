package dyvil.tuple;

import java.util.Objects;

import dyvil.annotation.ClassParameters;
import dyvil.annotation.Covariant;
import dyvil.collection.Cell;

@ClassParameters(names = { "_1", "_2", "_3" })
public class Tuple3<@Covariant A, @Covariant B, @Covariant C> implements Cell<A, B, C>
{
	private static final long serialVersionUID = -1770703458034946695L;
	
	public final A	_1;
	public final B	_2;
	public final C	_3;
	
	public static <A, B, C> Tuple3<A, B, C> apply(A _1, B _2, C _3)
	{
		return new Tuple3<A, B, C>(_1, _2, _3);
	}
	
	public Tuple3(A _1, B _2, C _3)
	{
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
	}
	
	@Override
	public A getRow()
	{
		return this._1;
	}
	
	@Override
	public B getColumn()
	{
		return this._2;
	}
	
	@Override
	public C getValue()
	{
		return this._3;
	}
	
	@Override
	public Tuple3<A, B, C> toTuple()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "(" + this._1 + ", " + this._2 + ", " + this._3 + ")";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Cell))
		{
			return false;
		}
		Cell cell = (Cell) obj;
		return Objects.equals(this._1, cell.getRow()) && Objects.equals(this._2, cell.getColumn()) && Objects.equals(this._3, cell.getValue());
	}
	
	@Override
	public int hashCode()
	{
		return Cell.cellHashCode(this);
	}
}
