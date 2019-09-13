package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractHashSet;
import dyvil.lang.LiteralConvertible;
import dyvil.util.ImmutableException;

import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
@Immutable
public class HashSet<E> extends AbstractHashSet<E> implements ImmutableSet<E>
{
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private HashSet<E> set;

		public Builder()
		{
			this.set = new HashSet<>();
		}

		public Builder(int capacity)
		{
			this.set = new HashSet<>(capacity);
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
			HashSet<E> set = this.set;
			this.set = null;
			return set;
		}
	}

	private static final long serialVersionUID = -1698577535888129119L;

	// Factory Methods

	@NonNull
	@SafeVarargs
	public static <E> HashSet<E> apply(@NonNull E... elements)
	{
		return new HashSet<>(elements);
	}

	@NonNull
	public static <E> HashSet<E> from(E @NonNull [] array)
	{
		return new HashSet<>(array);
	}

	@NonNull
	public static <E> HashSet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return new HashSet<>(iterable);
	}

	@NonNull
	public static <E> HashSet<E> from(@NonNull SizedIterable<? extends E> iterable)
	{
		return new HashSet<>(iterable);
	}

	@NonNull
	public static <E> HashSet<E> from(@NonNull Set<? extends E> set)
	{
		return new HashSet<>(set);
	}

	@NonNull
	public static <E> HashSet<E> from(@NonNull AbstractHashSet<? extends E> hashSet)
	{
		return new HashSet<>(hashSet);
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

	protected HashSet()
	{
		super();
	}

	protected HashSet(int capacity)
	{
		super(capacity);
	}

	public HashSet(E @NonNull [] elements)
	{
		super(elements);
	}

	public HashSet(@NonNull Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public HashSet(@NonNull SizedIterable<? extends E> iterable)
	{
		super(iterable);
	}

	public HashSet(@NonNull Set<? extends E> set)
	{
		super(set);
	}

	public HashSet(@NonNull AbstractHashSet<? extends E> hashSet)
	{
		super(hashSet);
	}

	// Implementation Methods

	@Override
	protected void addElement(int hash, E element, int index)
	{
		this.elements[index] = new HashElement<>(element, hash, this.elements[index]);
		this.size++;
	}

	@Override
	protected void removeElement(@NonNull HashElement<E> element)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Set");
	}

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		HashSet<E> newSet = new HashSet<>(this);
		newSet.ensureCapacityInternal(this.size + 1);
		newSet.addInternal(element);
		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> removed(@Nullable Object element)
	{
		HashSet<E> newSet = new HashSet<>(this.size);

		for (E element1 : this)
		{
			if (element1 != element && (element == null || !element.equals(element1)))
			{
				newSet.addInternal(element1);
			}
		}
		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		HashSet<E> newSet = new HashSet<>(this.size);

		for (E element1 : this)
		{
			if (!collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}

		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<>(this.size);

		for (E element1 : this)
		{
			if (collection.contains(element1))
			{
				newSet.addInternal(element1);
			}
		}

		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<>(this);
		newSet.ensureCapacity(this.size + collection.size());
		for (E element : collection)
		{
			newSet.addInternal(element);
		}
		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<>(this.size + collection.size());

		for (E element : this)
		{
			if (!collection.contains(element))
			{
				newSet.addInternal(element);
			}
		}

		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		HashSet<R> newSet = new HashSet<>(this.size);

		for (E element : this)
		{
			newSet.addInternal(mapper.apply(element));
		}
		return newSet;
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		HashSet<R> newSet = new HashSet<>(this.size << 2);

		for (E element : this)
		{
			for (R newElement : mapper.apply(element))
			{
				newSet.addInternal(newElement);
			}
		}
		newSet.flatten();
		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		HashSet<E> newSet = new HashSet<>(this.size);

		for (E element : this)
		{
			if (predicate.test(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}

	@NonNull
	@Override
	public ImmutableSet<E> copy()
	{
		return new HashSet<>(this);
	}

	@NonNull
	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.HashSet<>(this);
	}

	@Override
	public java.util.@NonNull Set<E> toJava()
	{
		return java.util.Collections.unmodifiableSet(super.toJava());
	}
}
