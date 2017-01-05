package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromTuple
@Immutable
public class SingletonSet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = 4398163898648791092L;

	private transient E element;

	@NonNull
	public static <E> SingletonSet<E> apply(E element)
	{
		return new SingletonSet<>(element);
	}

	public SingletonSet(E element)
	{
		this.element = element;
	}

	@Override
	public int size()
	{
		return 1;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new SingletonIterator<>(this.element);
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
		action.accept(this.element);
	}

	@Override
	public boolean contains(Object element)
	{
		return Objects.equals(element, this.element);
	}

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		return ImmutableSet.apply(this.element, element);
	}

	@Override
	public ImmutableSet<E> removed(Object element)
	{
		if (Objects.equals(this.element, element))
		{
			return ImmutableSet.apply();
		}
		return ImmutableSet.apply(this.element);
	}

	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		if (collection.contains(this.element))
		{
			return ImmutableSet.apply();
		}
		return ImmutableSet.apply(this.element);
	}

	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		if (!collection.contains(this.element))
		{
			return ImmutableSet.apply();
		}
		return this;
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		if (!collection.contains(this))
		{
			return ImmutableSet.from(collection);
		}

		Object[] array = new Object[1 + collection.size()];
		int index = 1;
		array[0] = this.element;
		outer:
		for (E element : collection)
		{
			for (int i = 1; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}

			array[index++] = element;
		}
		return new ArraySet<>((E[]) array, index, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		if (!collection.contains(this.element))
		{
			return ImmutableSet.from(collection);
		}

		Object[] array = new Object[1 + collection.size()];
		int index = 1;
		array[0] = this.element;
		outer:
		for (E element : collection)
		{
			for (int i = 1; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}

			array[index++] = element;
		}
		return new ArraySet<>((E[]) array, index, true);
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return ImmutableSet.apply(mapper.apply(this.element));
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return ImmutableSet.from(mapper.apply(this.element));
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return ImmutableSet.apply(this.element);
		}
		return ImmutableSet.apply();
	}

	@Override
	public Object @NonNull [] toArray()
	{
		return new Object[] { this.element };
	}

	@Override
	public E @NonNull [] toArray(@NonNull Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, 1);
		array[0] = type.cast(this.element);
		return array;
	}

	@Override
	public void toArray(int index, Object @NonNull [] store)
	{
		store[index] = this.element;
	}

	@NonNull
	@Override
	public ImmutableSet<E> copy()
	{
		return ImmutableSet.apply(this.element);
	}

	@Override
	public <RE> MutableSet<RE> emptyCopy()
	{
		return MutableSet.apply();
	}

	@NonNull
	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return MutableSet.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableSet<E> mutable()
	{
		return MutableSet.apply(this.element);
	}

	@NonNull
	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return ImmutableSet.builder();
	}

	@NonNull
	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return ImmutableSet.builder(capacity);
	}

	@Override
	public java.util.@NonNull Set<E> toJava()
	{
		return Collections.singleton(this.element);
	}

	@NonNull
	@Override
	@SuppressWarnings("StringBufferReplaceableByString")
	public String toString()
	{
		final String elementToString = String.valueOf(this.element);

		// No String concat to make use of the known length
		return new StringBuilder(elementToString.length() + Collection.START_STRING.length() + Collection.END_STRING
			                                                                                       .length())
			       .append(Collection.START_STRING).append(elementToString).append(Collection.END_STRING).toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.writeObject(this.element);
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.element = (E) in.readObject();
	}
}
