package dyvil.collection.range.closed;

import dyvil.annotation.Immutable;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class LongRange implements dyvil.collection.range.LongRange
{
	private final long start;
	private final long end;

	public static LongRange apply(long from, long to)
	{
		return new LongRange(from, to);
	}

	public LongRange(long from, long to)
	{
		this.start = from;
		this.end = to;
	}

	@Override
	public boolean isHalfOpen()
	{
		return false;
	}

	@Override
	public dyvil.collection.range.LongRange asHalfOpen()
	{
		return new dyvil.collection.range.halfopen.LongRange(this.start, this.end);
	}

	@Override
	public dyvil.collection.range.LongRange asClosed()
	{
		return this;
	}

	@Override
	public Long first()
	{
		return this.start;
	}

	@Override
	public Long last()
	{
		return this.end;
	}

	@Override
	public int size()
	{
		return (int) (this.end - this.start + 1);
	}

	@Override
	public Iterator<Long> iterator()
	{
		return new Iterator<Long>()
		{
			private long value = LongRange.this.start;

			@Override
			public boolean hasNext()
			{
				return this.value <= LongRange.this.end;
			}

			@Override
			public Long next()
			{
				final long value = this.value;
				if (value > LongRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value++;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Long> action)
	{
		for (long i = this.start; i <= this.end; i++)
		{
			action.accept(i);
		}
	}

	@Override
	public boolean contains(long value)
	{
		return value >= this.start && value <= this.end;
	}

	@Override
	public long[] toLongArray()
	{
		final long[] result = new long[this.size()];

		int index = 0;

		for (long i = this.start; i <= this.end; i++)
		{
			result[index++] = i;
		}
		return result;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		for (long i = this.start; i <= this.end; i++)
		{
			store[index++] = i;
		}
	}

	@Override
	public dyvil.collection.range.LongRange copy()
	{
		return new LongRange(this.start, this.end);
	}

	@Override
	public String toString()
	{
		return this.start + " .. " + this.end;
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
