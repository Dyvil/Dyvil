package dyvil.collection.range.primitive;

import dyvil.collection.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class FloatRange implements Range<Float>
{
	private final float     start;
	private final float     end;
	private final float     increment;
	private final boolean halfOpen;

	public static FloatRange apply(float first, float last)
	{
		return new FloatRange(first, last);
	}

	public static FloatRange apply(float first, float last, float increment)
	{
		return new FloatRange(first, last, increment);
	}

	public static FloatRange halfOpen(float first, float last)
	{
		return new FloatRange(first, last, true);
	}

	public static FloatRange halfOpen(float first, float last, float increment)
	{
		return new FloatRange(first, last, increment, true);
	}

	public FloatRange(float first, float last)
	{
		this.start = first;
		this.end = last;
		this.increment = 1;
		this.halfOpen = false;
	}

	public FloatRange(float start, float end, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = 1;
		this.halfOpen = halfOpen;
	}

	public FloatRange(float start, float end, float increment)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = false;
	}

	public FloatRange(float start, float end, float increment, boolean halfOpen)
	{
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.halfOpen = halfOpen;
	}

	public FloatRange by(float increment)
	{
		return new FloatRange(this.start, this.end, increment, this.halfOpen);
	}

	public FloatRange closed()
	{
		return new FloatRange(this.start, this.end, this.increment, false);
	}

	public FloatRange halfOpen()
	{
		return new FloatRange(this.start, this.end, this.increment, true);
	}

	@Override
	public Float first()
	{
		return this.start;
	}

	@Override
	public Float last()
	{
		return this.halfOpen ? this.end - this.increment : this.end;
	}

	@Override
	public int count()
	{
		return (int) ((this.end - this.start) / this.increment) + (this.halfOpen ? 0 : 1);
	}

	@Override
	public boolean isHalfOpen()
	{
		return this.halfOpen;
	}

	@Override
	public Iterator<Float> iterator()
	{
		return new Iterator<Float>()
		{
			private float value = FloatRange.this.start;

			@Override
			public boolean hasNext()
			{
				if (FloatRange.this.halfOpen)
				{
					return this.value < FloatRange.this.end;
				}
				return this.value <= FloatRange.this.end;
			}

			@Override
			public Float next()
			{
				final float value = this.value;
				if (value > FloatRange.this.end || FloatRange.this.halfOpen && value == FloatRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value += FloatRange.this.increment;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Float> action)
	{
		if (this.halfOpen)
		{
			for (float i = this.start; i < this.end; i += this.increment)
			{
				action.accept(i);
			}
			return;
		}

		for (float i = this.start; i <= this.end; i += this.increment)
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

		final float value = ((Number) o).floatValue();
		return value >= this.start && (this.halfOpen ? value < this.end : value <= this.end);
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		if (this.halfOpen)
		{
			for (float i = this.start; i < this.end; i += this.increment)
			{
				store[(int) (i + index)] = i;
			}
			return;
		}

		for (float i = this.start; i <= this.end; i += this.increment)
		{
			store[(int) (i + index)] = i;
		}
	}

	@Override
	public FloatRange copy()
	{
		return new FloatRange(this.start, this.end, this.increment, this.halfOpen);
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
