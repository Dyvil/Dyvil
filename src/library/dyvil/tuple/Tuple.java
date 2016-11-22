package dyvil.tuple;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.ClassParameters;
import dyvil.annotation.internal.Covariant;
import dyvil.collection.Cell;
import dyvil.collection.Entry;

import java.util.Objects;

public class Tuple
{
	private Tuple()
	{
		// no instances
	}

	@ClassParameters(names = { "_1", "_2" })
	@Immutable
	public static class Of2<@Covariant T1, @Covariant T2> implements Entry<T1, T2>
	{
		private static final long serialVersionUID = 0xb8a7db86af7fabb0L;

		public final T1 _1;
		public final T2 _2;

		public static <T1, T2> Of2<T1, T2> apply(T1 _1, T2 _2)
		{
			return new Of2<>(_1, _2);
		}

		public Of2(T1 _1, T2 _2)
		{
			this._1 = _1;
			this._2 = _2;
		}

		@Override
		public T1 getKey()
		{
			return this._1;
		}

		@Override
		public T2 getValue()
		{
			return this._2;
		}

		@Override
		public Of2<T1, T2> toTuple()
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
			return Entry.entryEquals(this, obj);
		}

		@Override
		public int hashCode()
		{
			return Entry.entryHashCode(this);
		}
	}

	@ClassParameters(names = { "_1", "_2", "_3" })
	@Immutable
	public static class Of3<@Covariant T1, @Covariant T2, @Covariant T3> implements Cell<T1, T2, T3>
	{
		private static final long serialVersionUID = 0xe76d329625456d79L;

		public final T1 _1;
		public final T2 _2;
		public final T3 _3;

		public static <A, B, C> Of3<A, B, C> apply(A _1, B _2, C _3)
		{
			return new Of3<>(_1, _2, _3);
		}

		public Of3(T1 _1, T2 _2, T3 _3)
		{
			this._1 = _1;
			this._2 = _2;
			this._3 = _3;
		}

		@Override
		public T1 getRow()
		{
			return this._1;
		}

		@Override
		public T2 getColumn()
		{
			return this._2;
		}

		@Override
		public T3 getValue()
		{
			return this._3;
		}

		@Override
		public Of3<T1, T2, T3> toTuple()
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
			final Cell cell = (Cell) obj;
			return Objects.equals(this._1, cell.getRow()) && Objects.equals(this._2, cell.getColumn()) && Objects
					.equals(this._3, cell.getValue());
		}

		@Override
		public int hashCode()
		{
			return Cell.cellHashCode(this);
		}
	}
}
