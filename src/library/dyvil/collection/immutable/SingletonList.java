package dyvil.collection.immutable;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.lang.literal.TupleConvertible;
import dyvil.annotation.Immutable;

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

@TupleConvertible
@Immutable
public class SingletonList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -3612390510825873413L;
	
	private transient E element;
	
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
	
	@Override
	public Iterator<E> iterator()
	{
		return new SingletonIterator<>(this.element);
	}
	
	@Override
	public Iterator<E> reverseIterator()
	{
		return new SingletonIterator<>(this.element);
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		action.accept(this.element);
	}
	
	@Override
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return reducer.apply(initialValue, this.element);
	}
	
	@Override
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return reducer.apply(initialValue, this.element);
	}
	
	@Override
	public E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.element;
	}
	
	@Override
	public E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
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
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		return ImmutableList.apply(this.element, element);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return new PrependList<>(this.element, (ImmutableList<E>) ImmutableList.linked(collection));
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		if (Objects.equals(this.element, element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		if (collection.contains(this.element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		if (!collection.contains(this.element))
		{
			return (ImmutableList<E>) EmptyList.instance;
		}
		return this;
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new SingletonList<>(mapper.apply(this.element));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return ImmutableList.linked((Iterable<R>) mapper.apply(this.element));
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		if (condition.test(this.element))
		{
			return this;
		}
		return (ImmutableList<E>) EmptyList.instance;
	}
	
	@Override
	public ImmutableList<E> reversed()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		return this;
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		return this;
	}
	
	@Override
	public Object[] toArray()
	{
		return new Object[] { this.element };
	}
	
	@Override
	public E[] toArray(Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, 1);
		array[0] = type.cast(this.element);
		return array;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		store[index] = this.element;
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new SingletonList<>(this.element);
	}

	@Override
	public <RE> MutableList<RE> emptyCopy()
	{
		return MutableList.apply();
	}

	@Override
	public <RE> MutableList<RE> emptyCopy(int capacity)
	{
		return MutableList.withCapacity(capacity);
	}
	
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
	public java.util.List<E> toJava()
	{
		return Collections.singletonList(this.element);
	}
	
	@Override
	@SuppressWarnings("StringBufferReplaceableByString")
	public String toString()
	{
		final String elementToString = String.valueOf(this.element);
		// No String concat to make use of the known length
		return new StringBuilder(elementToString + 2).append('[').append(elementToString).append(']').toString();
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(this.element);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.element = (E) in.readObject();
	}
}
