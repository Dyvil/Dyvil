package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractIdentityHashSet;
import dyvil.lang.LiteralConvertible;

import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
@Immutable
public class IdentityHashSet<E> extends AbstractIdentityHashSet<E> implements ImmutableSet<E>
{
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private IdentityHashSet<E> set;

		public Builder()
		{
			this.set = new IdentityHashSet<>(DEFAULT_CAPACITY);
		}

		public Builder(int capacity)
		{
			this.set = new IdentityHashSet<>(capacity);
		}

		@Override
		public void add(E element)
		{
			if (this.set == null)
			{
				throw new IllegalStateException("Already built!");
			}

			this.set.addInternal(element);
		}

		@Override
		public ImmutableSet<E> build()
		{
			IdentityHashSet<E> set = this.set;
			this.set = null;
			return set;
		}
	}

	private static final long serialVersionUID = -1347044009183554635L;

	// Factory Methods

	@NonNull
	@SafeVarargs
	public static <E> IdentityHashSet<E> apply(@NonNull E... elements)
	{
		return new IdentityHashSet<>(elements);
	}

	@NonNull
	public static <E> IdentityHashSet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return new IdentityHashSet<>(iterable);
	}

	@NonNull
	public static <E> IdentityHashSet<E> from(@NonNull SizedIterable<? extends E> iterable)
	{
		return new IdentityHashSet<>(iterable);
	}

	@NonNull
	public static <E> IdentityHashSet<E> from(@NonNull Set<? extends E> iterable)
	{
		return new IdentityHashSet<>(iterable);
	}

	@NonNull
	public static <E> IdentityHashSet<E> from(@NonNull AbstractIdentityHashSet<? extends E> iterable)
	{
		return new IdentityHashSet<>(iterable);
	}

	@NonNull
	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	@NonNull
	public static <E> Builder<E> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	// Constructors

	protected IdentityHashSet()
	{
		super(DEFAULT_CAPACITY);
	}

	protected IdentityHashSet(int capacity)
	{
		super(capacity);
	}

	public IdentityHashSet(E @NonNull [] elements)
	{
		super(elements);
	}

	public IdentityHashSet(@NonNull Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public IdentityHashSet(@NonNull SizedIterable<? extends E> iterable)
	{
		super(iterable);
	}

	public IdentityHashSet(@NonNull Set<? extends E> set)
	{
		super(set);
	}

	public IdentityHashSet(@NonNull AbstractIdentityHashSet<? extends E> set)
	{
		super(set);
	}

	// Implementation Methods

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this);
		copy.ensureCapacity(this.size + 1);
		copy.addInternal(element);
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			if (element != e)
			{
				copy.addInternal(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			if (!collection.contains(e))
			{
				copy.addInternal(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			if (collection.contains(e))
			{
				copy.addInternal(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size + collection.size());
		for (E e : this)
		{
			copy.addInternal(e);
		}
		for (E e : collection)
		{
			copy.addInternal(e);
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size + collection.size());
		for (E e : this)
		{
			if (!collection.contains(e))
			{
				copy.addInternal(e);
			}
		}
		for (E e : collection)
		{
			if (!this.contains(e))
			{
				copy.addInternal(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		IdentityHashSet<R> copy = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			copy.addInternal(mapper.apply(e));
		}
		return copy;
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		IdentityHashSet<R> copy = new IdentityHashSet<>(this.size << 2);
		for (E e : this)
		{
			for (R result : mapper.apply(e))
			{
				copy.addInternal(result);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		IdentityHashSet<E> set = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			if (predicate.test(e))
			{
				set.addInternal(e);
			}
		}
		return set;
	}

	@NonNull
	@Override
	public ImmutableSet<E> copy()
	{
		return this.immutableCopy();
	}

	@NonNull
	@Override
	public MutableSet<E> mutable()
	{
		return this.mutableCopy();
	}
}
