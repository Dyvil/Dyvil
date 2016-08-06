package dyvil.util;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@LiteralConvertible.FromNil(methodName = "fromNil")
@LiteralConvertible.FromTuple
@DyvilModifiers(Modifiers.SEALED)
@Immutable
public interface Option<T> extends Serializable
{
	static Option fromNil()
	{
		return None.instance;
	}

	static <T> Option<T> of(T t)
	{
		return t == null ? None.instance : new Some<>(t);
	}

	static <T> Option<T> apply()
	{
		return (Option<T>) None.instance;
	}

	static <T> Option<T> apply(T t)
	{
		return new Some<>(t);
	}

	T get();

	boolean isPresent();

	void forEach(Consumer<? super T> consumer);

	Option<T> filter(Predicate<? super T> predicate);

	<U> Option<U> map(Function<? super T, ? extends U> function);

	<U> Option<U> flatMap(Function<? super T, Option<U>> function);

	T orElse(T value);

	T orElse(Supplier<? extends T> supplier);

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> T $bang(Option<T> option)
	{
		return option.get();
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> T $qmark$qmark(Option<T> option, T value)
	{
		return option.orElse(value);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <T> T $qmark$qmark(Option<T> option, Supplier<? extends T> supplier)
	{
		return option.orElse(supplier);
	}
}
