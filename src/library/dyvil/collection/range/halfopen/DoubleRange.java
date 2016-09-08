package dyvil.collection.range.halfopen;

import dyvil.annotation.Immutable;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class DoubleRange implements dyvil.collection.range.DoubleRange
{
	private final double start;
	private final double end;

	public static DoubleRange apply(double from, double to)
	{
		return new DoubleRange(from, to);
	}

	public DoubleRange(double from, double to)
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
	public dyvil.collection.range.DoubleRange asHalfOpen()
	{
		return this;
	}

	@Override
	public dyvil.collection.range.DoubleRange asClosed()
	{
		return new dyvil.collection.range.closed.DoubleRange(this.start, this.end);
	}

	@Override
	public Double first()
	{
		return this.start;
	}

	@Override
	public Double last()
	{
		return this.end - 1;
	}

	@Override
	public int size()
	{
		return (int) (this.end - this.start);
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
				return this.value < DoubleRange.this.end;
			}

			@Override
			public Double next()
			{
				final double value = this.value;
				if (value >= DoubleRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value++;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Double> action)
	{
		for (double i = this.start; i < this.end; i++)
		{
			action.accept(i);
		}
	}

	@Override
	public boolean contains(double value)
	{
		return value >= this.start && value < this.end;
	}

	@Override
	public double[] toDoubleArray()
	{
		final double[] result = new double[this.size()];
		int index = 0;

		for (double i = this.start; i < this.end; i++)
		{
			result[index++] = i;
		}
		return result;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		for (double i = this.start; i < this.end; i++)
		{
			store[index++] = i;
		}
	}

	@Override
	public dyvil.collection.range.DoubleRange copy()
	{
		return new DoubleRange(this.start, this.end);
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
