package dyvil.collection.range;

import dyvil.collection.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class LongRange implements Range<Long>
{
	private final long     start;
	private final long     end;
	private final long     increment;
	private final boolean halfOpen;

	public static LongRange apply(long first, long last)
	{
		return new LongRange(first, last);
	}

	public static LongRange apply(long first, long last, long increment)
	{
		return new LongRange(first, last, increment);
	}

	public static LongRange halfOpen(long first, long last)
	{
		return new LongRange(first, last, true);
	}

	public static LongRange halfOpen(long first, long last, long increment)
	{
		return new LongRange(first, last, increment, true);
	}

	public LongRange(long first, long last)
	{
		this.start = first;
		this.end = last;
		this.increment = 1;
		this.halfOpen = false;
	}

	public LongRange(long start, long end, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = 1;
		this.halfOpen = halfOpen;
	}

	public LongRange(long start, long end, long increment)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = false;
	}

	public LongRange(long start, long end, long increment, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = halfOpen;
	}

	public LongRange by(long increment)
	{
		return new LongRange(this.start, this.end, increment, this.halfOpen);
	}

	public LongRange closed()
	{
		return new LongRange(this.start, this.end, this.increment, false);
	}

	public LongRange halfOpen()
	{
		return new LongRange(this.start, this.end, this.increment, true);
	}

	@Override
	public Long first()
	{
		return this.start;
	}

	@Override
	public Long last()
	{
		return this.halfOpen ? this.end - this.increment : this.end;
	}

	@Override
	public int count()
	{
		return (int) this.longCount();
	}

	@Override
	public long longCount()
	{
		return (this.end - this.start) / this.increment + (this.halfOpen ? 0 : 1);
	}

	@Override
	public boolean isHalfOpen()
	{
		return this.halfOpen;
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
				if (LongRange.this.halfOpen)
				{
					return this.value < LongRange.this.end;
				}
				return this.value <= LongRange.this.end;
			}

			@Override
			public Long next()
			{
				final long value = this.value;
				if (value > LongRange.this.end || LongRange.this.halfOpen && value == LongRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value += LongRange.this.increment;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Long> action)
	{
		if (this.halfOpen)
		{
			for (long i = this.start; i < this.end; i += this.increment)
			{
				action.accept(i);
			}
			return;
		}

		for (long i = this.start; i <= this.end; i += this.increment)
		{
			action.accept(i);
		}
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof Number))
		{
			return false;
		}

		final long value = ((Number) o).longValue();
		return value >= this.start && (this.halfOpen ? value < this.end : value <= this.end);
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		if (this.halfOpen)
		{
			for (long i = this.start; i < this.end; i += this.increment)
			{
				store[(int) (i + index)] = i;
			}
			return;
		}

		for (long i = this.start; i <= this.end; i += this.increment)
		{
			store[(int) (i + index)] = i;
		}
	}

	@Override
	public LongRange copy()
	{
		return new LongRange(this.start, this.end, this.increment, this.halfOpen);
	}

	@Override
	public String toString()
	{
		return this.start + (this.halfOpen ? " ..< " : " .. ") + this.end;
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
