package dyvil.util;

import dyvil.annotation._internal.sealed;
import dyvil.lang.literal.NilConvertible;
import dyvil.lang.literal.TupleConvertible;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@NilConvertible
@TupleConvertible
public
@sealed
interface Option<T> extends Serializable
{
	static <T> Option<T> of(T t)
	{
		return t == null ? None.instance : new Some(t);
	}
	
	static <T> Option<T> apply()
	{
		return None.instance;
	}
	
	static <T> Option<T> apply(T t)
	{
		return new Some(t);
	}
	
	T $bang();
	
	boolean $qmark();
	
	void forEach(Consumer<? super T> paramConsumer);
	
	Option<T> filter(Predicate<? super T> paramPredicate);
	
	<U> Option<U> map(Function<? super T, ? extends U> paramFunction);
	
	<U> Option<U> flatMap(Function<? super T, Option<U>> paramFunction);
	
	T orElse(T paramT);
	
	T orElse(Supplier<? extends T> paramSupplier);
}
