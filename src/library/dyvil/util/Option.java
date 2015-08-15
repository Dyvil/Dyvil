package dyvil.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dyvil.lang.literal.NilConvertible;
import dyvil.lang.literal.TupleConvertible;

import dyvil.annotation.sealed;

@NilConvertible
@TupleConvertible
public abstract @sealed interface Option<T>
{
	public static <T> Option<T> of(T t)
	{
		return t == null ? None.instance : new Some(t);
	}
	
	public static <T> Option<T> apply()
	{
		return None.instance;
	}
	
	public static <T> Option<T> apply(T t)
	{
		return new Some(t);
	}
	
	public abstract T $bang();
	
	public abstract boolean $qmark();
	
	public abstract void forEach(Consumer<? super T> paramConsumer);
	
	public abstract Option<T> filter(Predicate<? super T> paramPredicate);
	
	public abstract <U> Option<U> map(Function<? super T, ? extends U> paramFunction);
	
	public abstract <U> Option<U> flatMap(Function<? super T, Option<U>> paramFunction);
	
	public abstract T orElse(T paramT);
	
	public abstract T orElse(Supplier<? extends T> paramSupplier);
}
