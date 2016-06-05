package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractHashSet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.util.ImmutableException;

import java.util.function.Function;
import java.util.function.Predicate;

@ArrayConvertible
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

	@SafeVarargs
	public static <E> HashSet<E> apply(E... elements)
	{
		return new HashSet<>(elements);
	}

	public static <E> HashSet<E> from(E[] array)
	{
		return new HashSet<>(array);
	}

	public static <E> HashSet<E> from(Iterable<? extends E> iterable)
	{
		return new HashSet<>(iterable);
	}

	public static <E> HashSet<E> from(Collection<? extends E> collection)
	{
		return new HashSet<>(collection);
	}

	public static <E> HashSet<E> from(Set<? extends E> set)
	{
		return new HashSet<>(set);
	}

	public static <E> HashSet<E> from(AbstractHashSet<? extends E> hashSet)
	{
		return new HashSet<>(hashSet);
	}

	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

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

	public HashSet(E[] elements)
	{
		super(elements);
	}

	public HashSet(Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public HashSet(Collection<? extends E> collection)
	{
		super(collection);
	}

	public HashSet(Set<? extends E> set)
	{
		super(set);
	}

	public HashSet(AbstractHashSet<? extends E> hashSet)
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
	protected void removeElement(HashElement<E> element)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Set");
	}

	@Override
	public ImmutableSet<E> added(E element)
	{
		HashSet<E> newSet = new HashSet<>(this);
		newSet.ensureCapacityInternal(this.size + 1);
		newSet.addInternal(element);
		return newSet;
	}

	@Override
	public ImmutableSet<E> removed(Object element)
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

	@Override
	public ImmutableSet<? extends E> difference(Collection<?> collection)
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

	@Override
	public ImmutableSet<? extends E> intersection(Collection<? extends E> collection)
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

	@Override
	public ImmutableSet<? extends E> union(Collection<? extends E> collection)
	{
		HashSet<E> newSet = new HashSet<>(this);
		newSet.ensureCapacity(this.size + collection.size());
		for (E element : collection)
		{
			newSet.addInternal(element);
		}
		return newSet;
	}

	@Override
	public ImmutableSet<? extends E> symmetricDifference(Collection<? extends E> collection)
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

	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		HashSet<R> newSet = new HashSet<>(this.size);

		for (E element : this)
		{
			newSet.addInternal(mapper.apply(element));
		}
		return newSet;
	}

	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
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

	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		HashSet<E> newSet = new HashSet<>(this.size);

		for (E element : this)
		{
			if (condition.test(element))
			{
				newSet.addInternal(element);
			}
		}
		return newSet;
	}

	@Override
	public ImmutableSet<E> copy()
	{
		return new HashSet<>(this);
	}

	@Override
	public MutableSet<E> mutable()
	{
		return new dyvil.collection.mutable.HashSet<>(this);
	}

	@Override
	public java.util.Set<E> toJava()
	{
		return java.util.Collections.unmodifiableSet(super.toJava());
	}
}
