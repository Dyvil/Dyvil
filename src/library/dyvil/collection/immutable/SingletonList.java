package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromTuple
@Immutable
public class SingletonList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -3612390510825873413L;

	private transient E element;

	@NonNull
	public static <E> SingletonList<E> apply(E element)
	{
		return new SingletonList<>(element);
	}

	public SingletonList(E element)
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

	@NonNull
	@Override
	public Iterator<E> reverseIterator()
	{
		return new SingletonIterator<>(this.element);
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
		action.accept(this.element);
	}

	@Override
	public <R> R foldLeft(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return reducer.apply(initialValue, this.element);
	}

	@Override
	public <R> R foldRight(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return reducer.apply(initialValue, this.element);
	}

	@Override
	public E reduceLeft(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.element;
	}

	@Override
	public E reduceRight(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.element;
	}

	@Override
	public boolean contains(Object element)
	{
		return Objects.equals(element, this.element);
	}

	@Override
	public E subscript(int index)
	{
		if (index == 0)
		{
			return this.element;
		}
		throw new IndexOutOfBoundsException("Index out of bounds for Singleton List");
	}

	@Override
	public E get(int index)
	{
		return index == 0 ? this.element : null;
	}

	@NonNull
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex > 0 || length > 0)
		{
			throw new IndexOutOfBoundsException("Slice out of range for Empty List");
		}
		return this;
	}

	@Override
	public int indexOf(Object element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}

	@Override
	public int lastIndexOf(Object element)
	{
		return Objects.equals(this.element, element) ? 0 : -1;
	}

	@NonNull
	@Override
	public ImmutableList<E> added(E element)
	{
		return ImmutableList.apply(this.element, element);
	}

	@NonNull
	@Override
	public ImmutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		return new PrependList<>(this.element, (ImmutableList<E>) ImmutableList.linked(collection));
	}

	@NonNull
	@Override
	public ImmutableList<E> removed(Object element)
	{
		if (Objects.equals(this.element, element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> difference(@NonNull Collection<?> collection)
	{
		if (collection.contains(this.element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> intersection(@NonNull Collection<? extends E> collection)
	{
		if (!collection.contains(this.element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return new SingletonList<>(mapper.apply(this.element));
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return ImmutableList.from(mapper.apply(this.element));
	}

	@NonNull
	@Override
	public ImmutableList<E> filtered(@NonNull Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return this;
		}
		return (ImmutableList<E>) EmptyList.instance;
	}

	@NonNull
	@Override
	public ImmutableList<E> reversed()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		return this;
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
	public ImmutableList<E> copy()
	{
		return new SingletonList<>(this.element);
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy()
	{
		return MutableList.apply();
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy(int capacity)
	{
		return MutableList.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableList<E> mutable()
	{
		return MutableList.apply(this.element);
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return ImmutableList.builder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return ImmutableList.builder(capacity);
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		return Collections.singletonList(this.element);
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
		return List.listEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return List.listHashCode(this);
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
