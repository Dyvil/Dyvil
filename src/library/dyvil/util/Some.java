package dyvil.util;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.ClassParameters;
import dyvil.lang.literal.TupleConvertible;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@TupleConvertible
@ClassParameters(names = { "value" })
@Immutable
public final class Some<T> implements Option<T>
{
	private static final long serialVersionUID = 4760957059219326387L;
	
	protected final T value;
	
	public static <T> Some<T> apply(T value)
	{
		return new Some<>(value);
	}
	
	public Some(T value)
	{
		this.value = value;
	}

	public T value()
	{
		return this.value;
	}
	
	@Override
	public T get()
	{
		return this.value;
	}
	
	@Override
	public boolean isPresent()
	{
		return true;
	}
	
	@Override
	public void forEach(Consumer<? super T> consumer)
	{
		consumer.accept(this.value);
	}
	
	@Override
	public Option<T> filter(Predicate<? super T> predicate)
	{
		return predicate.test(this.value) ? this : None.instance;
	}
	
	@Override
	public <U> Option<U> map(Function<? super T, ? extends U> mapper)
	{
		return new Some<>(mapper.apply(this.value));
	}
	
	@Override
	public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper)
	{
		return Objects.requireNonNull(mapper.apply(this.value));
	}
	
	@Override
	public T orElse(T other)
	{
		return this.value;
	}
	
	@Override
	public T orElse(Supplier<? extends T> other)
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		return this.value == null ? "Some(null)" : "Some(" + this.value.toString() + ')';
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Some))
		{
			return false;
		}

		Object otherValue = ((Some) obj).value;
		return otherValue == this.value || (this.value != null && this.value.equals(otherValue));
	}

	@Override
	public int hashCode()
	{
		return this.value == null ? 0 : 31 * this.value.hashCode();
	}
}
