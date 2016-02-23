package dyvil.collection.range.primitive;

import dyvil.collection.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class IntRange implements Range<Integer>
{
	private final int     start;
	private final int     end;
	private final int     increment;
	private final boolean halfOpen;

	public static IntRange apply(int first, int last)
	{
		return new IntRange(first, last);
	}

	public static IntRange apply(int first, int last, int increment)
	{
		return new IntRange(first, last, increment);
	}

	public static IntRange halfOpen(int first, int last)
	{
		return new IntRange(first, last, true);
	}

	public static IntRange halfOpen(int first, int last, int increment)
	{
		return new IntRange(first, last, increment, true);
	}

	public IntRange(int first, int last)
	{
		this.start = first;
		this.end = last;
		this.increment = 1;
		this.halfOpen = false;
	}

	public IntRange(int start, int end, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = 1;
		this.halfOpen = halfOpen;
	}

	public IntRange(int start, int end, int increment)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = false;
	}

	public IntRange(int start, int end, int increment, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = halfOpen;
	}

	public IntRange by(int increment)
	{
		return new IntRange(this.start, this.end, increment, this.halfOpen);
	}

	public IntRange closed()
	{
		return new IntRange(this.start, this.end, this.increment, false);
	}

	public IntRange halfOpen()
	{
		return new IntRange(this.start, this.end, this.increment, true);
	}

	@Override
	public Integer first()
	{
		return this.start;
	}

	@Override
	public Integer last()
	{
		return this.halfOpen ? this.end - this.increment : this.end;
	}

	@Override
	public int count()
	{
		return (this.end - this.start) / this.increment + (this.halfOpen ? 0 : 1);
	}

	@Override
	public boolean isHalfOpen()
	{
		return this.halfOpen;
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
				if (IntRange.this.halfOpen)
				{
					return this.value < IntRange.this.end;
				}
				return this.value <= IntRange.this.end;
			}

			@Override
			public Integer next()
			{
				final int value = this.value;
				if (value > IntRange.this.end || IntRange.this.halfOpen && value == IntRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value += IntRange.this.increment;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Integer> action)
	{
		if (this.halfOpen)
		{
			for (int i = this.start; i < this.end; i += this.increment)
			{
				action.accept(i);
			}
			return;
		}

		for (int i = this.start; i <= this.end; i += this.increment)
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

		final int value = ((Number) o).intValue();
		return value >= this.start && (this.halfOpen ? value < this.end : value <= this.end);
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		if (this.halfOpen)
		{
			for (int i = this.start; i < this.end; i += this.increment)
			{
				store[i + index] = i;
			}
			return;
		}

		for (int i = this.start; i <= this.end; i += this.increment)
		{
			store[i + index] = i;
		}
	}

	@Override
	public IntRange copy()
	{
		return new IntRange(this.start, this.end, this.increment, this.halfOpen);
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
