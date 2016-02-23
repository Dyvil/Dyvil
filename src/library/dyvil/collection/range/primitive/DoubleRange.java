package dyvil.collection.range.primitive;

import dyvil.collection.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class DoubleRange implements Range<Double>
{
	private final double     start;
	private final double     end;
	private final double     increment;
	private final boolean halfOpen;

	public static DoubleRange apply(double first, double last)
	{
		return new DoubleRange(first, last);
	}

	public static DoubleRange apply(double first, double last, double increment)
	{
		return new DoubleRange(first, last, increment);
	}

	public static DoubleRange halfOpen(double first, double last)
	{
		return new DoubleRange(first, last, true);
	}

	public static DoubleRange halfOpen(double first, double last, double increment)
	{
		return new DoubleRange(first, last, increment, true);
	}

	public DoubleRange(double first, double last)
	{
		this.start = first;
		this.end = last;
		this.increment = 1;
		this.halfOpen = false;
	}

	public DoubleRange(double start, double end, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = 1;
		this.halfOpen = halfOpen;
	}

	public DoubleRange(double start, double end, double increment)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = false;
	}

	public DoubleRange(double start, double end, double increment, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = halfOpen;
	}

	public DoubleRange by(double increment)
	{
		return new DoubleRange(this.start, this.end, increment, this.halfOpen);
	}

	public DoubleRange closed()
	{
		return new DoubleRange(this.start, this.end, this.increment, false);
	}

	public DoubleRange halfOpen()
	{
		return new DoubleRange(this.start, this.end, this.increment, true);
	}

	@Override
	public Double first()
	{
		return this.start;
	}

	@Override
	public Double last()
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
		return (long) ((this.end - this.start) / this.increment) + (this.halfOpen ? 0 : 1);
	}

	@Override
	public boolean isHalfOpen()
	{
		return this.halfOpen;
	}

	@Override
	public Iterator<Double> iterator()
	{
		return new Iterator<Double>()
		{
			private double value = DoubleRange.this.start;

			@Override
			public boolean hasNext()
			{
				if (DoubleRange.this.halfOpen)
				{
					return this.value < DoubleRange.this.end;
				}
				return this.value <= DoubleRange.this.end;
			}

			@Override
			public Double next()
			{
				final double value = this.value;
				if (value > DoubleRange.this.end || DoubleRange.this.halfOpen && value == DoubleRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value += DoubleRange.this.increment;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Double> action)
	{
		if (this.halfOpen)
		{
			for (double i = this.start; i < this.end; i += this.increment)
			{
				action.accept(i);
			}
			return;
		}

		for (double i = this.start; i <= this.end; i += this.increment)
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

		final double value = ((Number) o).doubleValue();
		return value >= this.start && (this.halfOpen ? value < this.end : value <= this.end);
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		if (this.halfOpen)
		{
			for (double i = this.start; i < this.end; i += this.increment)
			{
				store[(int) (i + index)] = i;
			}
			return;
		}

		for (double i = this.start; i <= this.end; i += this.increment)
		{
			store[(int) (i + index)] = i;
		}
	}

	@Override
	public DoubleRange copy()
	{
		return new DoubleRange(this.start, this.end, this.increment, this.halfOpen);
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
