package dyvil.lang;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Option<T>
{
	public static <T> Option<T> of(T t)
	{
		return t == null ? None.instance : new Some(t);
	}
	
	public static <T> Option<T> ofNullable(T t)
	{
		return new Some(t);
	}
	
	public static <T> Option<T> apply(T t)
	{
		return new Some(t);
	}
	
	public static <T> Option<T> none()
	{
		return None.instance;
	}
	
	public T get();
	
	public boolean isEmpty();
	
	public default boolean isDefined()
	{
		return !isEmpty();
	}
	
	public void ifPresent(Consumer<? super T> consumer);
	
	public Option<T> filter(Predicate<? super T> predicate);
	
	public <U> Option<U> map(Function<? super T, ? extends U> mapper);
	
	public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper);
	
	public T orElse(T other);
	
	public T orElse(Supplier<? extends T> other);
}
