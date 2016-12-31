package dyvil.collection.range.halfopen;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Range;
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
	private static final long serialVersionUID = -8656379367421762895L;

	protected transient T first;
	protected transient T last;

	@NonNull
	public static <T extends Rangeable<T>> ObjectRange<T> apply(T first, T last)
	{
		return new ObjectRange<>(first, last);
	}

	public ObjectRange(T first, T last)
	{
		this.first = first;
		this.last = last;
	}

	@Override
	public boolean isHalfOpen()
	{
		return true;
	}

	@Override
	public @NonNull Range<T> asHalfOpen()
	{
		return this;
	}

	@Override
	public @NonNull Range<T> asClosed()
	{
		return new dyvil.collection.range.closed.ObjectRange<>(this.first, this.last);
	}

	@Override
	public T first()
	{
		return this.first;
	}

	@Override
	public T last()
	{
		return this.last.previous();
	}

	@Override
	public int size()
	{
		return this.first.distanceTo(this.last);
	}

	@NonNull
	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private T current = ObjectRange.this.first;

			@Override
			public T next()
			{
				if (this.current.compareTo(ObjectRange.this.last) >= 0)
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
				return this.current.compareTo(ObjectRange.this.last) < 0;
			}

			@NonNull
			@Override
			public String toString()
			{
				return "RangeIterator(" + ObjectRange.this + ")";
			}
		};
	}

	@Override
	public void forEach(@NonNull Consumer<? super T> action)
	{
		for (T current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			action.accept(current);
		}
	}

	@Override
	public void toArray(int index, Object @NonNull [] store)
	{
		for (T current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			store[index++] = current;
		}
	}

	@Override
	public boolean contains(Object o)
	{
		for (Rangeable<T> current = this.first; current.compareTo(this.last) < 0; current = current.next())
		{
			//noinspection unchecked
			if (current.compareTo((T) o) == 0)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public @NonNull Range<T> copy()
	{
		return new ObjectRange<>(this.first, this.last);
	}

	@NonNull
	@Override
	public String toString()
	{
		return this.first + " ..< " + this.last;
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

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeObject(this.first);
		out.writeObject(this.last);
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.first = (T) in.readObject();
		this.last = (T) in.readObject();
	}
}
