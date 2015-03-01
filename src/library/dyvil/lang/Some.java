package dyvil.lang;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Some<T> implements Option<T>
{
	private final T	value;
	
	public Some(T value)
	{
		this.value = value;
	}
	
	public static <T> Some<T> apply(T t)
	{
		return new Some(t);
	}
	
	@Override
	public T get()
	{
		return this.value;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public boolean isDefined()
	{
		return true;
	}
	
	@Override
	public void ifPresent(Consumer<? super T> consumer)
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
		return new Some(mapper.apply(this.value));
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
		return "Some(" + this.value + ")";
	}
	
	@Override
	public int hashCode()
	{
		return 31 + (this.value == null ? 0 : this.value.hashCode());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Some))
		{
			return false;
		}
		Some other = (Some) obj;
		if (this.value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!this.value.equals(other.value))
		{
			return false;
		}
		return true;
	}
}
