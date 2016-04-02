package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractIdentityHashSet;
import dyvil.lang.literal.ArrayConvertible;

import java.util.function.Function;
import java.util.function.Predicate;

@ArrayConvertible
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

	@SafeVarargs
	public static <E> IdentityHashSet<E> apply(E... elements)
	{
		return new IdentityHashSet<>(elements);
	}

	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	public static <E> Builder<E> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	protected IdentityHashSet()
	{
		super(DEFAULT_CAPACITY);
	}
	
	protected IdentityHashSet(int capacity)
	{
		super(capacity);
	}
	
	public IdentityHashSet(Collection<E> collection)
	{
		super(collection);
	}
	
	public IdentityHashSet(Set<E> set)
	{
		super(set);
	}
	
	public IdentityHashSet(AbstractIdentityHashSet<E> set)
	{
		super(set);
	}
	
	@SafeVarargs
	public IdentityHashSet(E... elements)
	{
		super(elements);
	}
	
	@Override
	public ImmutableSet<E> added(E element)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this);
		copy.ensureCapacity(this.size + 1);
		copy.addInternal(element);
		return copy;
	}
	
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
	
	@Override
	public ImmutableSet<? extends E> difference(Collection<?> collection)
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
	
	@Override
	public ImmutableSet<? extends E> intersection(Collection<? extends E> collection)
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
	
	@Override
	public ImmutableSet<? extends E> union(Collection<? extends E> collection)
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
	
	@Override
	public ImmutableSet<? extends E> symmetricDifference(Collection<? extends E> collection)
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
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		IdentityHashSet<R> copy = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			copy.addInternal(mapper.apply(e));
		}
		return copy;
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
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
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		IdentityHashSet<E> set = new IdentityHashSet<>(this.size);
		for (E e : this)
		{
			if (condition.test(e))
			{
				set.addInternal(e);
			}
		}
		return set;
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return this.immutableCopy();
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return this.mutableCopy();
	}
}
