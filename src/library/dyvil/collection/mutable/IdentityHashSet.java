package dyvil.collection.mutable;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.collection.impl.AbstractIdentityHashSet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.function.Function;
import java.util.function.Predicate;

import static dyvil.collection.impl.AbstractIdentityHashMap.*;

@NilConvertible
@ArrayConvertible
public class IdentityHashSet<E> extends AbstractIdentityHashSet<E>implements MutableSet<E>
{
	private static final long serialVersionUID = 5634688694810236366L;
	
	private float			loadFactor;
	private transient int	threshold;
	
	public static <E> IdentityHashSet<E> apply()
	{
		return new IdentityHashSet();
	}
	
	public static <E> IdentityHashSet<E> apply(E... elements)
	{
		return new IdentityHashSet(elements);
	}
	
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
	
	public IdentityHashSet(Collection<E> collection)
	{
		super(collection);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length);
	}
	
	public IdentityHashSet(AbstractIdentityHashSet<E> set)
	{
		super(set);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length);
	}
	
	public IdentityHashSet(E... elements)
	{
		super(elements);
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.updateThreshold(this.table.length);
	}
	
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
	public void map(Function<? super E, ? extends E> mapper)
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
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		IdentityHashSet<E> copy = new IdentityHashSet<E>(this.size, this.loadFactor);
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
	public void filter(Predicate<? super E> condition)
	{
		for (int i = 0; i < this.table.length; i++)
		{
			Object o = this.table[i];
			if (o != null && !condition.test((E) unmaskNull(o)))
			{
				this.table[i] = null;
			}
		}
	}
	
	@Override
	public MutableSet<E> copy()
	{
		return new IdentityHashSet<E>(this);
	}
	
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new IdentityHashSet<R>(this.size);
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		return new dyvil.collection.immutable.IdentityHashSet<E>(this);
	}
}
