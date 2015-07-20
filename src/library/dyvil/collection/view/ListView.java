package dyvil.collection.view;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.ImmutableIterator;

public class ListView<E> implements ImmutableList<E>
{
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
	
	@Override
	public Iterator< E> iterator()
	{
		return this.list.isImmutable() ? this.list.iterator() : new ImmutableIterator(this.list.iterator());
	}
	
	@Override
	public Iterator< E> reverseIterator()
	{
		return this.list.isImmutable() ? this.list.reverseIterator() : new ImmutableIterator(this.list.reverseIterator());
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		this.list.forEach(action);
	}
	
	@Override
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super  E, ? extends R> reducer)
	{
		return this.list.foldLeft(initialValue, reducer);
	}
	
	@Override
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super  E, ? extends R> reducer)
	{
		return this.list.foldRight(initialValue, reducer);
	}
	
	@Override
	public  E reduceLeft(BiFunction<? super  E, ? super  E, ? extends  E> reducer)
	{
		return this.list.reduceLeft(reducer);
	}
	
	@Override
	public  E reduceRight(BiFunction<? super  E, ? super  E, ? extends  E> reducer)
	{
		return this.list.reduceRight(reducer);
	}
	
	@Override
	public  E subscript(int index)
	{
		return this.list.subscript(index);
	}
	
	@Override
	public  E get(int index)
	{
		return this.list.get(index);
	}
	
	@Override
	public ImmutableList< E> subList(int startIndex, int length)
	{
		return new ListView(this.list.subList(startIndex, length));
	}
	
	@Override
	public ImmutableList< E> $plus( E element)
	{
		return new ListView(this.list.$plus(element));
	}
	
	@Override
	public ImmutableList<? extends  E> $plus$plus(Collection<? extends  E> collection)
	{
		return new ListView(this.list.$plus$plus(collection));
	}
	
	@Override
	public ImmutableList< E> $minus(Object element)
	{
		return new ListView(this.list.$minus(element));
	}
	
	@Override
	public ImmutableList<? extends  E> $minus$minus(Collection<?> collection)
	{
		return new ListView(this.list.$minus$minus(collection));
	}
	
	@Override
	public ImmutableList<? extends  E> $amp(Collection<? extends  E> collection)
	{
		return new ListView(this.list.$amp(collection));
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super  E, ? extends R> mapper)
	{
		return new ListView(this.list.mapped(mapper));
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super  E, ? extends Iterable<? extends R>> mapper)
	{
		return new ListView(this.list.flatMapped(mapper));
	}
	
	@Override
	public ImmutableList< E> filtered(Predicate<? super  E> condition)
	{
		return new ListView(this.list.filtered(condition));
	}
	
	@Override
	public ImmutableList<E> reversed()
	{
		return new ListView(this.list.reversed());
	}
	
	@Override
	public ImmutableList< E> sorted()
	{
		return new ListView(this.list.sorted());
	}
	
	@Override
	public ImmutableList< E> sorted(Comparator<? super  E> comparator)
	{
		return new ListView(this.list.sorted(comparator));
	}
	
	@Override
	public ImmutableList< E> distinct()
	{
		return new ListView(this.list.distinct());
	}
	
	@Override
	public ImmutableList< E> distinct(Comparator<? super  E> comparator)
	{
		return new ListView(this.list.distinct());
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
	
	@Override
	public ImmutableList< E> copy()
	{
		return new ListView(this.list.copy());
	}
	
	@Override
	public MutableList< E> mutable()
	{
		return this.list.mutable();
	}
	
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
