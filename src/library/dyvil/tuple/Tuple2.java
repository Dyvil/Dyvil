package dyvil.tuple;

import dyvil.annotation._internal.ClassParameters;
import dyvil.annotation._internal.Covariant;
import dyvil.collection.Entry;

import java.util.Objects;

@ClassParameters(names = { "_1", "_2" })
public class Tuple2<@Covariant A, @Covariant B> implements Entry<A, B>
{
	private static final long serialVersionUID = -5140899078103651408L;
	
	public final A _1;
	public final B _2;
	
	public static <A, B> Tuple2<A, B> apply(A _1, B _2)
	{
		return new Tuple2<A, B>(_1, _2);
	}
	
	public Tuple2(A _1, B _2)
	{
		this._1 = _1;
		this._2 = _2;
	}
	
	@Override
	public A getKey()
	{
		return this._1;
	}
	
	@Override
	public B getValue()
	{
		return this._2;
	}
	
	@Override
	public Tuple2<A, B> toTuple()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "(" + this._1 + ", " + this._2 + ")";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Entry))
		{
			return false;
		}
		Entry entry = (Entry) obj;
		return Objects.equals(this._1, entry.getKey()) && Objects.equals(this._2, entry.getValue());
	}
	
	@Override
	public int hashCode()
	{
		return Entry.entryHashCode(this);
	}
}
