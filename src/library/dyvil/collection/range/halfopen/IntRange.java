package dyvil.collection.range.halfopen;

import dyvil.annotation.Immutable;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class IntRange implements dyvil.collection.range.IntRange
{
	private final int start;
	private final int end;

	public static dyvil.collection.range.IntRange apply(int from, int to)
	{
		return new IntRange(from, to);
	}

	public IntRange(int from, int to)
	{
		this.start = from;
		this.end = to;
	}

	@Override
	public boolean isHalfOpen()
	{
		return true;
	}

	@Override
	public dyvil.collection.range.IntRange asHalfOpen()
	{
		return this;
	}

	@Override
	public dyvil.collection.range.IntRange asClosed()
	{
		return new dyvil.collection.range.closed.IntRange(this.start, this.end);
	}

	@Override
	public Integer first()
	{
		return this.start;
	}

	@Override
	public Integer last()
	{
		return this.end - 1;
	}

	@Override
	public int size()
	{
		return this.end - this.start;
	}

	@Override
	public Iterator<Integer> iterator()
	{
		return new Iterator<Integer>()
		{
			private int value = IntRange.this.start;

			@Override
			public boolean hasNext()
			{
				return this.value < IntRange.this.end;
			}

			@Override
			public Integer next()
			{
				final int value = this.value;
				if (value >= IntRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value++;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Integer> action)
	{
		for (int i = this.start; i < this.end; i++)
		{
			action.accept(i);
		}
	}

	@Override
	public boolean contains(int value)
	{
		return this.start <= value && value < this.end;
	}

	@Override
	public int[] toIntArray()
	{
		final int[] result = new int[this.size()];
		int index = 0;

		for (int i = this.start; i < this.end; i++)
		{
			result[index++] = i;
		}
		return result;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		for (int i = this.start; i < this.end; i++)
		{
			store[index++] = i;
		}
	}

	@Override
	public dyvil.collection.range.IntRange copy()
	{
		return new IntRange(this.start, this.end);
	}

	@Override
	public String toString()
	{
		return this.start + " ..< " + this.end;
	}

	@Override
	public boolean equals(Object obj)
	{
		return Range.rangeEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return Range.rangeHashCode(this);
	}
}
