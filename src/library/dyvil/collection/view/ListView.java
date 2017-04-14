package dyvil.collection.view;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.ImmutableIterator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Immutable
public class ListView<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -4432411036304678600L;

	protected final List<E> list;

	public ListView(List<E> list)
	{
		this.list = list;
	}

	@Override
	public int size()
	{
		return this.list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return this.list.isImmutable() ? this.list.iterator() : new ImmutableIterator<>(this.list.iterator());
	}

	@NonNull
	@Override
	public Iterator<E> reverseIterator()
	{
		return this.list.isImmutable() ?
			       this.list.reverseIterator() :
			       new ImmutableIterator<>(this.list.reverseIterator());
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
		this.list.forEach(action);
	}

	@Override
	public <R> R foldLeft(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return this.list.foldLeft(initialValue, reducer);
	}

	@Override
	public <R> R foldRight(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return this.list.foldRight(initialValue, reducer);
	}

	@Override
	public E reduceLeft(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.list.reduceLeft(reducer);
	}

	@Override
	public E reduceRight(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.list.reduceRight(reducer);
	}

	@Override
	public E subscript(int index)
	{
		return this.list.subscript(index);
	}

	@Override
	public E get(int index)
	{
		return this.list.get(index);
	}

	@NonNull
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		return new ListView<>(this.list.subList(startIndex, length));
	}

	@NonNull
	@Override
	public ImmutableList<E> added(E element)
	{
		return new ListView<>(this.list.added(element));
	}

	@NonNull
	@Override
	public ImmutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		return new ListView<>(this.list.union(collection));
	}

	@NonNull
	@Override
	public ImmutableList<E> removed(Object element)
	{
		return new ListView<>(this.list.removed(element));
	}

	@NonNull
	@Override
	public ImmutableList<E> difference(@NonNull Collection<?> collection)
	{
		return new ListView<>(this.list.difference(collection));
	}

	@NonNull
	@Override
	public ImmutableList<E> intersection(@NonNull Collection<? extends E> collection)
	{
		return new ListView<>(this.list.intersection(collection));
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return new ListView<>(this.list.mapped(mapper));
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return new ListView<>(this.list.flatMapped(mapper));
	}

	@NonNull
	@Override
	public ImmutableList<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		return new ListView<>(this.list.filtered(predicate));
	}

	@NonNull
	@Override
	public ImmutableList<E> reversed()
	{
		return new ListView<>(this.list.reversed());
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		return new ListView<>(this.list.sorted());
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		return new ListView<>(this.list.sorted(comparator));
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct()
	{
		return new ListView<>(this.list.distinct());
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		return new ListView<>(this.list.distinct());
	}

	@Override
	public int indexOf(Object element)
	{
		return this.list.indexOf(element);
	}

	@Override
	public int lastIndexOf(Object element)
	{
		return this.list.lastIndexOf(element);
	}

	@NonNull
	@Override
	public ImmutableList<E> copy()
	{
		return new ListView<>(this.list.copy());
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy()
	{
		return this.list.emptyCopy();
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy(int capacity)
	{
		return this.list.emptyCopy(capacity);
	}

	@NonNull
	@Override
	public MutableList<E> mutable()
	{
		return this.list.mutable();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return this.list.immutableBuilder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return this.list.immutableBuilder(capacity);
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		return this.list.isImmutable() ? this.list.toJava() : Collections.unmodifiableList(this.list.toJava());
	}

	@NonNull
	@Override
	public String toString()
	{
		return "view " + this.list.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.list.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return this.list.hashCode();
	}
}
