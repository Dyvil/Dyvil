package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.SizedIterable;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.collection.impl.AbstractIdentityHashSet;
import dyvil.lang.LiteralConvertible;

import java.util.function.Function;
import java.util.function.Predicate;

import static dyvil.collection.impl.AbstractIdentityHashMap.*;

@LiteralConvertible.FromArray
public class IdentityHashSet<E> extends AbstractIdentityHashSet<E> implements MutableSet<E>
{
	private static final long serialVersionUID = 5634688694810236366L;

	private           float loadFactor;
	private transient int   threshold;

	// Factory Methods

	@NonNull
	public static <E> IdentityHashSet<E> apply()
	{
		return new IdentityHashSet<>();
	}

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

	// Constructors

	public IdentityHashSet()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public IdentityHashSet(int capacity)
	{
		this(capacity, DEFAULT_LOAD_FACTOR);
	}

	public IdentityHashSet(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}

	public IdentityHashSet(int capacity, float loadFactor)
	{
		super(capacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}

		this.loadFactor = loadFactor;
		this.threshold = (int) Math.min(capacity * loadFactor, AbstractHashMap.MAX_ARRAY_SIZE + 1);
	}

	public IdentityHashSet(E @NonNull [] elements)
	{
		super(elements);
		this.defaultLoadFactor();
	}

	public IdentityHashSet(@NonNull Iterable<? extends E> iterable)
	{
		super(iterable);
		this.defaultLoadFactor();
	}

	public IdentityHashSet(@NonNull SizedIterable<? extends E> iterable)
	{
		super(iterable);
		this.defaultLoadFactor();
	}

	public IdentityHashSet(@NonNull Set<? extends E> set)
	{
		super(set);
		this.defaultLoadFactor();
	}

	public IdentityHashSet(@NonNull AbstractIdentityHashSet<? extends E> identityHashSet)
	{
		super(identityHashSet);
		this.defaultLoadFactor();
	}

	private void defaultLoadFactor()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length);
	}

	// Implementation Methods

	@Override
	protected void updateThreshold(int newCapacity)
	{
		this.threshold = (int) (newCapacity * this.loadFactor);
	}

	@Override
	public void clear()
	{
		this.size = 0;
		for (int i = 0; i < this.table.length; i++)
		{
			this.table[i] = null;
		}
	}

	@Override
	public boolean add(E element)
	{
		return this.addInternal(element);
	}

	@Override
	protected void addElement(int index, Object element)
	{
		this.table[index] = element;
		if (++this.size >= this.threshold)
		{
			this.flatten();
		}
	}

	@Override
	public boolean remove(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);

		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				this.size--;
				tab[i] = null;
				this.closeDeletion(i);
				return true;
			}
			if (item == null)
			{
				return false;
			}
			i = nextIndex(i, len);
		}
	}

	private void closeDeletion(int index)
	{
		Object[] tab = this.table;
		int len = tab.length;

		Object item;
		for (int i = nextIndex(index, len); (item = tab[i]) != null; i = nextIndex(i, len))
		{
			int r = index(item, len);
			if (i < r && (r <= index || index <= i) || r <= index && index <= i)
			{
				tab[index] = item;
				tab[i] = null;
				index = i;
			}
		}
	}

	@Override
	public void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		for (int i = 0; i < this.table.length; i++)
		{
			Object o = this.table[i];
			if (o != null)
			{
				this.table[i] = maskNull(mapper.apply((E) unmaskNull(o)));
			}
		}
	}

	@Override
	public void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<>(this.size, this.loadFactor);
		for (E element : this)
		{
			for (E result : mapper.apply(element))
			{
				copy.addInternal(result);
			}
		}

		this.table = copy.table;
		this.size = copy.size;
		this.threshold = copy.threshold;
	}

	@Override
	public void filter(@NonNull Predicate<? super E> predicate)
	{
		for (int i = 0; i < this.table.length; i++)
		{
			Object o = this.table[i];
			if (o != null && !predicate.test((E) unmaskNull(o)))
			{
				this.table[i] = null;
			}
		}
	}

	@NonNull
	@Override
	public MutableSet<E> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableSet<E> immutable()
	{
		return this.immutableCopy();
	}
}
