package dyvil.collection;

import java.util.Iterator;
import java.util.function.BiFunction;

public interface BidiQueryable<T> extends Queryable<T>
{
	@Override
	public Iterator<T> iterator();
	
	public Iterator<T> reverseIterator();
	
	@Override
	public default <R> R fold(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer)
	{
		return this.foldLeft(initialValue, reducer);
	}
	
	@Override
	public default T reduce(BiFunction<? super T, ? super T, ? extends T> reducer)
	{
		return this.reduceLeft(reducer);
	}
	
	public default <R> R foldLeft(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer)
	{
		Iterator<T> iterator = this.iterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default <R> R foldRight(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer)
	{
		Iterator<T> iterator = this.reverseIterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default T reduceLeft(BiFunction<? super T, ? super T, ? extends T> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<T> iterator = this.iterator();
		T initialValue = iterator.next();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default T reduceRight(BiFunction<? super T, ? super T, ? extends T> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<T> iterator = this.reverseIterator();
		T initialValue = iterator.next();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
}
