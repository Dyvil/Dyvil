package dyvil.collection.range.closed;

import dyvil.annotation.Immutable;
import dyvil.collection.Range;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class FloatRange implements dyvil.collection.range.FloatRange
{
	private final float start;
	private final float end;

	public static FloatRange apply(float from, float to)
	{
		return new FloatRange(from, to);
	}

	public FloatRange(float from, float to)
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
	public dyvil.collection.range.FloatRange asClosed()
	{
		return this;
	}

	@Override
	public dyvil.collection.range.FloatRange asHalfOpen()
	{
		return new dyvil.collection.range.halfopen.FloatRange(this.start, this.end);
	}

	@Override
	public Float first()
	{
		return this.start;
	}

	@Override
	public Float last()
	{
		return this.end;
	}

	@Override
	public int size()
	{
		return (int) (this.end - this.start + 1);
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
				return this.value <= FloatRange.this.end;
			}

			@Override
			public Float next()
			{
				final float value = this.value;
				if (value > FloatRange.this.end)
				{
					throw new NoSuchElementException();
				}

				this.value++;
				return value;
			}
		};
	}

	@Override
	public void forEach(Consumer<? super Float> action)
	{
		for (float i = this.start; i <= this.end; i++)
		{
			action.accept(i);
		}
	}

	@Override
	public boolean contains(float value)
	{
		return value >= this.start && value <= this.end;
	}

	@Override
	public float[] toFloatArray()
	{
		final float[] result = new float[this.size()];

		int index = 0;

		for (float i = this.start; i <= this.end; i++)
		{
			result[index++] = i;
		}
		return result;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		for (float i = this.start; i <= this.end; i++)
		{
			store[index++] = i;
		}
	}

	@Override
	public dyvil.collection.range.FloatRange copy()
	{
		return new FloatRange(this.start, this.end);
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
