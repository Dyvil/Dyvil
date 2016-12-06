package dyvil.collection.range.closed;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.Covariant;
import dyvil.collection.range.Rangeable;
import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@LiteralConvertible.FromTuple
@Immutable
public class ObjectRange<@Covariant T extends Rangeable<T>> implements dyvil.collection.Range<T>
{
	private static final long serialVersionUID = -2752505419258591804L;

	protected transient T first;
	protected transient T last;

	public static <T extends Rangeable<T>> ObjectRange apply(T from, T to)
	{
		return new ObjectRange<>(from, to);
	}

	public ObjectRange(T from, T to)
	{
		this.first = from;
		this.last = to;
	}

	@Override
	public dyvil.collection.Range asClosed()
	{
		return this;
	}

	@Override
	public dyvil.collection.Range asHalfOpen()
	{
		return new dyvil.collection.range.halfopen.ObjectRange<>(this.first, this.last);
	}

	@Override
	public T first()
	{
		return this.first;
	}

	@Override
	public T last()
	{
		return this.last;
	}

	@Override
	public int size()
	{
		return this.first.distanceTo(this.last) + 1;
	}

	@Override
	public boolean isHalfOpen()
	{
		return false;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private T current = ObjectRange.this.first;

			@Override
			public T next()
			{
				if (this.current.compareTo(ObjectRange.this.last) > 0)
				{
					throw new NoSuchElementException("End of Range");
				}

				final T c = this.current;
				this.current = this.current.next();
				return c;
			}

			@Override
			public boolean hasNext()
			{
				return this.current.compareTo(ObjectRange.this.last) <= 0;
			}

			@Override
			public String toString()
			{
				return "RangeIterator(" + ObjectRange.this + ")";
			}
		};
	}

	@Override
	public void forEach(Consumer<? super T> action)
	{
		for (T current = this.first; current.compareTo(this.last) <= 0; current = current.next())
		{
			action.accept(current);
		}
	}

	@Override
	public void toArray(int index, Object[] store)
	{
		for (T current = this.first; current.compareTo(this.last) <= 0; current = current.next())
		{
			store[index++] = current;
		}
	}

	@Override
	public boolean contains(Object o)
	{
		for (Rangeable<T> current = this.first; current.compareTo(this.last) <= 0; current = current.next())
		{
			if (current.compareTo((T) o) == 0)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public dyvil.collection.Range copy()
	{
		return new ObjectRange<>(this.first, this.last);
	}

	@Override
	public String toString()
	{
		return this.first + " .. " + this.last;
	}

	@Override
	public boolean equals(Object obj)
	{
		return dyvil.collection.Range.rangeEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return dyvil.collection.Range.rangeHashCode(this);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeObject(this.first);
		out.writeObject(this.last);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.first = (T) in.readObject();
		this.last = (T) in.readObject();
	}
}
