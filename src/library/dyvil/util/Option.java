package dyvil.util;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.NilConvertible;
import dyvil.lang.literal.TupleConvertible;
import dyvil.reflect.Modifiers;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@NilConvertible
@TupleConvertible
@DyvilModifiers(Modifiers.SEALED)
public interface Option<T> extends Serializable
{
	static <T> Option<T> of(T t)
	{
		return t == null ? None.instance : new Some<>(t);
	}
	
	static <T> Option<T> apply()
	{
		return None.instance;
	}
	
	static <T> Option<T> apply(T t)
	{
		return new Some<>(t);
	}
	
	T get();

	default T $bang()
	{
		return this.get();
	}
	
	boolean isPresent();

	default boolean $qmark()
	{
		return this.isPresent();
	}
	
	void forEach(Consumer<? super T> consumer);
	
	Option<T> filter(Predicate<? super T> predicate);
	
	<U> Option<U> map(Function<? super T, ? extends U> function);
	
	<U> Option<U> flatMap(Function<? super T, Option<U>> function);
	
	T orElse(T elseValue);

	default T $qmark$qmark(T elseValue)
	{
		return this.orElse(elseValue);
	}
	
	T orElse(Supplier<? extends T> elseSupplier);

	default T $qmark$qmark(Supplier<? extends T> elseSupplier)
	{
		return this.orElse(elseSupplier);
	}
}
