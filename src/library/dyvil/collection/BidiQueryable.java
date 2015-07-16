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
	
	public <R> R foldLeft(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer);
	
	public <R> R foldRight(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer);
	
	public T reduceLeft(BiFunction<? super T, ? super T, ? extends T> reducer);
	
	public T reduceRight(BiFunction<? super T, ? super T, ? extends T> reducer);
}
